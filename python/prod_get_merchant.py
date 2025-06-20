"""Solution: API 3.0"""

import sys
import time

from utils.system_wide import *
from utils.merchant_data import Merchant

from selenium import webdriver
from selenium.common.exceptions import NoSuchElementException, ElementNotInteractableException, TimeoutException
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.wait import WebDriverWait

class InvalidLogin(Exception):
    pass

class BraspagInternalServerError(Exception):
    pass

class EstablishmentNotFound(Exception):
    pass

class Automation:
    driver: webdriver.Chrome
    args: SystemArguments
    wait: WebDriverWait

    def __init__(self):
        options = webdriver.ChromeOptions()
        options.add_argument("--headless")
        options.add_argument("--disable-gpu")
        options.add_argument("--no-sandbox")
        self.driver = webdriver.Chrome(options=options)
        self.args = SystemArguments()
        self.wait = WebDriverWait(self.driver, timeout=10)

    def loading_state(self):
        loading_element_found = True
        while loading_element_found:
            try:
                loading_element = self.driver.find_element(by=By.ID, value="fancybox-loading")
                loading_element = EC.presence_of_element_located(loading_element)
            except NoSuchElementException:
                loading_element_found = False

    def check_title(self):
        if "500 - Internal server error" in self.driver.title:
            raise BraspagInternalServerError()

    def login(self):
        time.sleep(10)
        self.driver.get("https://admin.braspag.com.br/Admin/Home")

        username_input = self.wait.until(EC.element_to_be_clickable((By.ID, "param1")))
        password_input = self.wait.until(EC.element_to_be_clickable((By.ID, "param2")))
        submit_button = self.wait.until(EC.element_to_be_clickable((By.ID, "enter")))

        self.check_title()

        username_input.clear()
        username_input.send_keys(self.args.username)
        password_input.clear()
        password_input.send_keys(self.args.password)
        submit_button.click()
        time.sleep(1)

        self.check_title()

    def search_ec(self):
        self.driver.get("https://admin.braspag.com.br/EcommerceCielo/List")

        ec_input = self.wait.until(EC.element_to_be_clickable((By.ID, "EcNumber")))
        start_date_input = self.wait.until(EC.element_to_be_clickable((By.ID, "StartDate")))
        search_button = self.wait.until(EC.element_to_be_clickable((By.ID, "buttonSearch")))

        self.check_title()

        ec_input.clear()
        ec_input.send_keys(self.args.ec)
        start_date_input.clear()
        search_button.click()
        time.sleep(1)

        self.loading_state()

        adm_title_list = self.wait.until(EC.presence_of_all_elements_located((By.CLASS_NAME, "adm-title")))
        query_results_element = adm_title_list[1]
        query_results_text = query_results_element.get_attribute("innerText")

        if not query_results_text or not "1 registro(s)" in query_results_text:
            raise EstablishmentNotFound()

        link_elements = self.wait.until(EC.presence_of_all_elements_located((By.TAG_NAME, "a")))

        for link_element in link_elements:
            title_attribute = link_element.get_attribute("title")
            if title_attribute and "Ver Detalhes" in title_attribute:
                link_element.click()
                time.sleep(1)
                return

        raise NoSuchElementException()

    def get_merchant_data(self):
        merchant = Merchant()
        merchant.ec = self.args.ec
        form_control_static_elements = self.wait.until(EC.presence_of_all_elements_located((By.CLASS_NAME, "form-control-static")))
        merchant.mid = form_control_static_elements[1].get_attribute("innerText")
        document_element = form_control_static_elements[2].get_attribute("innerText")

        if document_element and len(document_element) == 14:
            merchant.document_type = "CPF"
        elif document_element and len(document_element) == 18:
            merchant.document_type = "CNPJ"
        else:
            merchant.document_type = "UNKNOWN"

        merchant.document_number = document_element
        merchant.name = form_control_static_elements[3].get_attribute("innerText")

        if merchant.document_type == "CPF":
            created_at_text = form_control_static_elements[9].get_attribute("innerText")

            if created_at_text:
                merchant.created_at = created_at_text.split("(")[0].strip()

        elif merchant.document_type == "CNPJ":
            merchant.created_at = form_control_static_elements[10].get_attribute("innerText")
        else:
            merchant.created_at = "N/A"

        check_box_list = self.wait.until(EC.presence_of_all_elements_located((By.CLASS_NAME, "check-box")))

        if check_box_list[0].get_attribute("checked"):
            merchant.blocked = True
        else:
            merchant.blocked = False

        pix_button = self.wait.until(EC.presence_of_element_located((By.ID, "buttonEditCieloPix")))
        pix_text = pix_button.get_attribute("innerText")

        if pix_text and pix_text == " Desabilitar":
            merchant.pix_enabled = True
        else:
            merchant.pix_enabled = False

        all_list_elements = self.wait.until(EC.presence_of_all_elements_located((By.TAG_NAME, "li")))

        antifraud_text = all_list_elements[101].get_attribute("innerText")

        if antifraud_text and antifraud_text == "Habilitado":
            merchant.antifraud_enabled = True
        else:
            merchant.antifraud_enabled = False

        tokenization_text = all_list_elements[102].get_attribute("innerText")

        if tokenization_text:
            enabled_text = tokenization_text.split(" ")[0]
            tokenization_key = tokenization_text.split(" ")[1]

            if "definido" in tokenization_key.lower():
                merchant.tokenization_enabled = False
            elif enabled_text == "Habilitado" and not "definido" in tokenization_key.lower():
                merchant.tokenization_enabled = True
            else:
                merchant.tokenization_enabled = False

        velocity_text = all_list_elements[103].get_attribute("innerText")

        if velocity_text:
            enabled_text = velocity_text.split(" ")[0]
            velocity_key = velocity_text.split(" ")[1]

            if "definido" in velocity_key.lower():
                merchant.velocity_enabled = False
            elif enabled_text == "Habilitado" and not "definido" in velocity_key.lower():
                merchant.velocity_enabled = True
            else:
                merchant.velocity_enabled = False

        recurrent_text = all_list_elements[104].get_attribute("innerText")

        if recurrent_text:
            if recurrent_text == "Habilitado":
                merchant.smart_recurrency_enabled = True
            else:
                merchant.smart_recurrency_enabled = False

        zero_auth_text = all_list_elements[106].get_attribute("innerText")

        if zero_auth_text:
            if zero_auth_text == "Zero Auth Habilitado":
                merchant.zero_dollar_auth_enabled = True
            else:
                merchant.zero_dollar_auth_enabled = False

        bin_query_text = all_list_elements[107].get_attribute("innerText")

        if bin_query_text:
            if bin_query_text == "Consulta Bin Habilitado":
                merchant.bin_query_enabled = True
            else:
                merchant.bin_query_enabled = False

        selective_auth_text = all_list_elements[108].get_attribute("innerText")

        if selective_auth_text:
            if selective_auth_text == "Autenticação seletiva Habilitado":
                merchant.selective_auth_enabled = True
            else:
                merchant.selective_auth_enabled = False

        automatic_cancellation_text = all_list_elements[109].get_attribute("innerText")

        if automatic_cancellation_text:
            if automatic_cancellation_text == "Desfazimento Automático Habilitado":
                merchant.try_automatic_cancellation_enabled = True
            else:
                merchant.try_automatic_cancellation_enabled = False

        force_braspag_auth_text = all_list_elements[110].get_attribute("innerText")

        if force_braspag_auth_text:
            if force_braspag_auth_text == "Forçar Autenticação Braspag Auth Habilitado":
                merchant.force_braspag_auth_enabled = True
            else:
                merchant.force_braspag_auth_enabled = False

        mtls_text = all_list_elements[111].get_attribute("innerText")

        if mtls_text:
            if mtls_text == "MTLS Habilitado":
                merchant.mtls_enabled = True
            else:
                merchant.mtls_enabled = False

        merchant_webhook = self.check_notification_url(merchant.mid)

        if merchant_webhook:
            if merchant_webhook != "":
                merchant.webhook_enabled = True
            else:
                merchant.webhook_enabled = False

        merchant.white_list_ip_count = self.get_ips_count(merchant.mid)
        merchant.write_to_json_file()

    def check_notification_url(self, mid):
        notification_url_path = f"https://admin.braspag.com.br/Transactional/Notifications/{mid}"
        self.driver.get(notification_url_path)
        self.check_title()
        notification_input = self.wait.until(EC.presence_of_element_located((By.ID, "notificationUrl")))
        return notification_input.get_attribute("innerText")

    def get_ips_count(self, mid):
        checker_url_path = f"https://admin.braspag.com.br/IpManager/EditReliableIp?merchantId={mid}"
        self.driver.get(checker_url_path)
        self.check_title()
        results_text_element = self.wait.until(EC.presence_of_all_elements_located((By.CLASS_NAME, "adm-title")))[1]
        ips_count_text = results_text_element.get_attribute("innerText")

        if ips_count_text:
            number_of_ips_str = ips_count_text.split("Resultado da Busca: ")[1].split(" ")[0]
            try:
                return int(number_of_ips_str)
            except ValueError:
                return 0
        else:
            return 0



def execute_attempt():
    result_string = ""
    retry = False
    finished_with_sucess = False
    automation = Automation()

    try:
        automation.login()
        automation.search_ec()
        automation.get_merchant_data()
        result_string = "Result is in JSON file"
        finished_with_sucess = True
    except TimeoutException:
        result_string = "Error finding element due to timeout"
        retry = True
    except InvalidLogin:
        result_string = "Invalid credentials"
        retry = False
    except EstablishmentNotFound:
        result_string = "EC not found"
        retry = False
    except BraspagInternalServerError:
        result_string = "Braspag internal error"
        retry = True
    except MissingRequiredArgs:
        result_string = "Missing required arguments"
        retry = False
    except InvalidUsernameLength:
        result_string = "Invalid username length"
        retry = False
    except InvalidPasswordLength:
        result_string = "Invalid password length"
        retry = False
    except InvalidEcLength:
        result_string = "Invalid ec length"
        retry = False
    except InvalidEc:
        result_string = "Invalid ec"
        retry = False
    except NoSuchElementException:
        result_string = "Could not find a element"
        retry = True
    finally:
        if automation.driver:
                automation.driver.quit()
        return (finished_with_sucess, retry, result_string)


def main():
    print("Initializing..")
    max_attemps = 3
    current_attempt = 0
    attempt_result = None

    while current_attempt < max_attemps:
        attempt_result = execute_attempt()
        sucess = attempt_result[0]
        can_retry= attempt_result[1]
        result_str = attempt_result[2]

        if sucess:
            print(result_str)
            sys.exit(0)

        # Assume failure from this point
        if not can_retry:
            print(result_str)
            sys.exit(1)
        else:
            # Assume failure, but can retry
            # Continue loop
            print(result_str)

        current_attempt += 1

    # If code goes here = max attempts reached
    if attempt_result is not None:
        result_str = attempt_result[2]
        print(result_str)
        sys.exit(1)
    else:
        print("Unexpected error")
        sys.exit(1)


if __name__ == "__main__":
    main()