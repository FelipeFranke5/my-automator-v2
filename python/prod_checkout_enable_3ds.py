"""Solution: Checkoout"""

import sys
import time

from utils.system_wide import *

from selenium import webdriver
from selenium.common.exceptions import NoSuchElementException, TimeoutException
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.wait import WebDriverWait

class InvalidLogin(Exception):
    pass

class BraspagInternalServerError(Exception):
    pass

class EstablishmentNotFound(Exception):
    pass

class MerchantIsBlocked(Exception):
    pass

class MerchantElementsNotFound(Exception):
    pass

class AlreadyEnabled(Exception):
    pass

class CouldNotSave(Exception):
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

    def find_merchant(self):
        cielo_link = self.wait.until(EC.element_to_be_clickable((By.ID, "cielo")))
        cielo_link.click()
        time.sleep(1)
        checkout_cielo_ecs_link = self.wait.until(EC.element_to_be_clickable((By.ID, "checkoutCieloEcs")))
        checkout_cielo_ecs_link.click()
        time.sleep(1)
        self.driver.switch_to.window(self.driver.window_handles[1])

        affiliation_code_input = self.wait.until(EC.element_to_be_clickable((By.ID, "AffiliationCode")))
        affiliation_code_input.clear()
        affiliation_code_input.send_keys(self.args.ec)

        start_created_date_input = self.wait.until(EC.element_to_be_clickable((By.ID, "StartCreatedDate")))
        start_created_date_input.clear()

        button_search = self.wait.until(EC.element_to_be_clickable((By.ID, "buttonSearch")))
        button_search.click()
        time.sleep(1)

        title_elements = self.wait.until(EC.presence_of_all_elements_located((By.CLASS_NAME, "title")))
        found_results_text = False

        self.check_title()

        for title_element in title_elements:
            inner_text = title_element.get_attribute("innerText")

            if inner_text and "Resultado da Busca" in inner_text:
                found_results_text = True

                if "0" in inner_text:
                    raise EstablishmentNotFound()
                
                merchant_status = self.wait.until(EC.presence_of_all_elements_located((By.TAG_NAME, "td")))[2]
                merchant_status_inner_text = merchant_status.get_attribute("innerText")

                if merchant_status_inner_text and "Bloqueado" in merchant_status_inner_text:
                    raise MerchantIsBlocked()
                
                merchant_link_table = self.wait.until(EC.presence_of_all_elements_located((By.TAG_NAME, "td")))[0]
                merchant_link = merchant_link_table.find_elements(By.TAG_NAME, "a")[0]
                merchant_link.click()
                time.sleep(1)

        if not found_results_text:
            raise MerchantElementsNotFound()
        
    def secure_3ds_is_enabled(self):
        self.check_title()
        check_boxes = self.wait.until(EC.presence_of_all_elements_located((By.CLASS_NAME, "check-box")))
        secure_3ds_check_box = check_boxes[2]
        secure_3ds_enabled = secure_3ds_check_box.get_attribute("checked") is not None
        return secure_3ds_enabled
    
    def find_payment_methods(self):
        if self.secure_3ds_is_enabled():
            raise AlreadyEnabled()
        
        merchant_id = self.wait.until(EC.presence_of_all_elements_located((By.CLASS_NAME, "form-row")))[0].get_attribute("innerText")

        if not merchant_id:
            raise MerchantElementsNotFound()
        
        merchant_id = merchant_id.split("\n")[1]
        url = f"https://adminv2.pagador.com.br/SolucaoIntegrada/EditMerchantTransactionalConfigurationPaymentMethods/{merchant_id}"
        self.driver.get(url)
        
    def enable_secure_3d(self):
        secure_3d_checkbox = self.wait.until(EC.element_to_be_clickable((By.ID, "CreditCardAuthenticationRequired")))
        secure_3d_checkbox.click()
        time.sleep(1)
        save_button = self.wait.until(EC.element_to_be_clickable((By.XPATH, "//input[@type='submit']")))
        save_button.click()
        time.sleep(1)

        if not self.secure_3ds_is_enabled():
            raise CouldNotSave()



def execute_attempt():
    result_string = ""
    retry = False
    finished_with_sucess = False
    automation = Automation()

    try:
        automation.login()
        automation.find_merchant()
        automation.find_payment_methods()
        automation.enable_secure_3d()
        result_string = "3DS enabled!"
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
    except MerchantIsBlocked:
        result_string = "Merchant is Blocked"
        retry = False
    except MerchantElementsNotFound:
        result_string = "Merchant Elements Not Found"
        retry = True
    except AlreadyEnabled:
        result_string = "3DS Already Enabled"
        retry = False
    except CouldNotSave:
        result_string = "Error while trying to save"
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
