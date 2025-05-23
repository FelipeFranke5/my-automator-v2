import sys
import time
from dataclasses import dataclass
from typing import Optional

from selenium import webdriver
from selenium.common.exceptions import NoSuchElementException, ElementNotInteractableException
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.wait import WebDriverWait

# -------------------- ENTITIES --------------------

@dataclass
class MerchantCredentials:
    """Entity representing merchant credentials."""
    username: str
    password: str
    merchant_id: str


# -------------------- USE CASES --------------------

class PixSetupUseCase:
    """Use case for setting up PIX for a merchant."""

    def __init__(self, repository):
        self.repository = repository

    def execute(self, credentials: MerchantCredentials) -> bool:
        """Execute the PIX setup process."""
        try:
            self.repository.login(credentials)
            pix_enabled = self.repository.enable_pix_if_needed(credentials)
            self.repository.setup_pix_affiliation(credentials)
            return True
        except Exception as e:
            print(f"Error during PIX setup: {str(e)}")
            return False


# -------------------- INTERFACE ADAPTERS --------------------

class MerchantRepository:
    """Interface for merchant repository operations."""

    def login(self, credentials: MerchantCredentials) -> bool:
        """Login to the admin portal."""
        pass

    def enable_pix_if_needed(self, credentials: MerchantCredentials) -> bool:
        """Enable PIX if it's not already enabled."""
        pass

    def setup_pix_affiliation(self, credentials: MerchantCredentials) -> bool:
        """Setup PIX affiliation."""
        pass


# -------------------- FRAMEWORKS & DRIVERS --------------------

class SeleniumMerchantRepository(MerchantRepository):
    """Implementation of MerchantRepository using Selenium."""

    def __init__(self, driver):
        self.driver = driver
        self.wait = WebDriverWait(driver, timeout=10)

    def login(self, credentials: MerchantCredentials) -> bool:
        """Login to the admin portal using Selenium."""
        self.driver.get("https://adminsandbox.braspag.com.br/Admin/Home")

        username_input = self.driver.find_element(by=By.ID, value="param1")
        password_input = self.driver.find_element(by=By.ID, value="param2")
        submit_button = self.driver.find_element(by=By.ID, value="enter")

        all_visible = [
            EC.element_to_be_clickable(username_input),
            EC.element_to_be_clickable(password_input),
            EC.element_to_be_clickable(submit_button)
        ]
        self.wait.until(EC.all_of(*all_visible))

        username_input.clear()
        username_input.send_keys(credentials.username)
        password_input.clear()
        password_input.send_keys(credentials.password)
        submit_button.click()

        time.sleep(1)
        admin_button = self.driver.find_element(by=By.ID, value="admin")
        self.wait.until(EC.element_to_be_clickable(admin_button))

        return True

    def enable_pix_if_needed(self, credentials: MerchantCredentials) -> bool:
        """Enable PIX if it's not already enabled."""
        self.driver.get(f"https://adminsandbox.braspag.com.br/EcommerceCielo/Details/{credentials.merchant_id}")
        time.sleep(1)

        button_edit_cielo_pix = self.driver.find_element(by=By.ID, value="buttonEditCieloPix")
        self.wait.until(EC.visibility_of(button_edit_cielo_pix))

        if button_edit_cielo_pix.get_attribute("innerText") == " Habilitar":
            print(f"PIX is currently disabled for {credentials.merchant_id}. Attempting to enable it")
            button_edit_cielo_pix.click()
            time.sleep(1)

            password_confirmation_input = self.driver.find_element(by=By.ID, value="Password")
            confirm_button = self.driver.find_element(by=By.ID, value="btnConfirm")

            form_is_loaded = [
                EC.element_to_be_clickable(password_confirmation_input),
                EC.element_to_be_clickable(confirm_button)
            ]
            self.wait.until(EC.all_of(*form_is_loaded))

            password_confirmation_input.clear()
            password_confirmation_input.send_keys(credentials.password)
            confirm_button.click()
            time.sleep(1)

            results = self.driver.find_elements(by=By.CLASS_NAME, value="modal-body")
            self.wait.until(EC.visibility_of(results[0]))

            if "sucesso" not in results[0].get_attribute("innerText"):
                raise Exception("Failed to enable PIX")

            print(f"PIX enabled for {credentials.merchant_id}")
            return True
        else:
            print(f"PIX is already enabled for {credentials.merchant_id}")
            return False

    def setup_pix_affiliation(self, credentials: MerchantCredentials) -> bool:
        """Setup PIX affiliation."""
        self.driver.get(f"https://adminsandbox.braspag.com.br/PagadorAffiliation/Edit?merchantId={credentials.merchant_id}&acquirerId=08b44818-ff47-4920-902f-fe498b881333")
        print("Accessing PIX affiliation")

        affiliation_code_input = self.driver.find_element(by=By.ID, value="Code")
        affiliation_password_input = self.driver.find_element(by=By.ID, value="Password")
        affiliation_save_button = self.driver.find_element(by=By.ID, value="buttonEditForm")

        affiliation_form_is_loaded = [
            EC.element_to_be_clickable(affiliation_code_input),
            EC.element_to_be_clickable(affiliation_password_input),
            EC.element_to_be_clickable(affiliation_save_button)
        ]
        self.wait.until(EC.all_of(*affiliation_form_is_loaded))

        if affiliation_code_input.get_attribute("value") == "2005":
            print("PIX is already setup")
            return True

        affiliation_code_input.clear()
        affiliation_code_input.send_keys("2005")
        affiliation_password_input.clear()
        affiliation_password_input.send_keys(credentials.password)
        affiliation_save_button.click()
        time.sleep(1)

        affiliation_results = self.driver.find_elements(by=By.CLASS_NAME, value="modal-body")
        self.wait.until(EC.visibility_of(affiliation_results[0]))

        if "sucesso" not in affiliation_results[0].get_attribute("innerText"):
            raise Exception("Failed to setup PIX affiliation")

        print("PIX setup completed")
        return True


# -------------------- MAIN CONTROLLER --------------------

class PixSetupController:
    """Controller for PIX setup."""

    def __init__(self, max_attempts: int = 3):
        self.max_attempts = max_attempts

    def create_driver(self):
        """Create a new webdriver instance."""
        options = webdriver.ChromeOptions()
        options.add_argument("--headless")
        options.add_argument("--disable-gpu")
        options.add_argument("--no-sandbox")
        return webdriver.Chrome(options=options)

    def parse_args(self, args) -> Optional[MerchantCredentials]:
        """Parse command line arguments."""
        try:
            username = args[1]
            password = args[2]
            merchant_id = args[3]
            return MerchantCredentials(username, password, merchant_id)
        except IndexError:
            print("Username/Password/MerchantId required!!!")
            return None

    def run(self, args: list) -> bool:
        """Run the PIX setup process."""
        print("PIX Sandbox setup initialized\n")
        print("First arg = Username")
        print("Second arg = Password")
        print("Third arg = MerchantId")
        print(f"Max attempts = {self.max_attempts}\n")

        credentials = self.parse_args(args)
        if not credentials:
            return False

        current_attempt = 1

        while current_attempt <= self.max_attempts:
            print(f"Current attempt = {current_attempt}")
            driver = None

            try:
                driver = self.create_driver()
                repository = SeleniumMerchantRepository(driver)
                use_case = PixSetupUseCase(repository)

                if use_case.execute(credentials):
                    return True

            except NoSuchElementException:
                print(f"Attempt number {current_attempt} has failed: Element not found")
            except ElementNotInteractableException:
                print(f"Attempt number {current_attempt} has failed: Element not interactable")
            except Exception as e:
                print(f"Attempt number {current_attempt} has failed: {str(e)}")
            finally:
                if driver:
                    driver.quit()

            current_attempt += 1
            time.sleep(1)

        print("All attempts failed")
        return False


# -------------------- MAIN ENTRY POINT --------------------

def main():
    """Main entry point."""
    print("Initializing..")
    controller = PixSetupController(max_attempts=3)
    success = controller.run(sys.argv)
    return 0 if success else 1


if __name__ == "__main__":
    sys.exit(main())