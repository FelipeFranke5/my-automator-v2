import json

class Merchant:
    ec: str
    mid: str | None
    document_type: str
    document_number: str | None
    name: str | None
    created_at: str | None
    blocked: bool
    pix_enabled: bool = False
    antifraud_enabled: bool = False
    tokenization_enabled: bool = False
    velocity_enabled: bool = False
    smart_recurrency_enabled: bool = False
    zero_dollar_auth_enabled: bool = False
    bin_query_enabled: bool = False
    selective_auth_enabled: bool = False
    try_automatic_cancellation_enabled: bool = False
    force_braspag_auth_enabled: bool = False
    mtls_enabled: bool = False
    webhook_enabled: bool = False
    white_list_ip_count: int = 0

    def __init__(self):
        pass

    def write_to_json_file(self):
        data = {
            "ec": self.ec,
            "mid": self.mid,
            "document_type": self.document_type,
            "document_number": self.document_number,
            "name": self.name,
            "created_at": self.created_at,
            "blocked": self.blocked,
            "pix_enabled": self.pix_enabled,
            "antifraud_enabled": self.antifraud_enabled,
            "tokenization_enabled": self.tokenization_enabled,
            "velocity_enabled": self.velocity_enabled,
            "smart_recurrency_enabled": self.smart_recurrency_enabled,
            "zero_dollar_auth_enabled": self.zero_dollar_auth_enabled,
            "bin_query_enabled": self.bin_query_enabled,
            "selective_auth_enabled": self.selective_auth_enabled,
            "try_automatic_cancellation_enabled": self.try_automatic_cancellation_enabled,
            "force_braspag_auth_enabled": self.force_braspag_auth_enabled,
            "mtls_enabled": self.mtls_enabled,
            "webhook_enabled": self.webhook_enabled,
            "white_list_ip_count": self.white_list_ip_count
        }

        file_path = f"{self.ec}.json"

        with open(file_path, "w") as json_file:
            json.dump(data, json_file, indent=4)


class CheckoutMerchant:
    ec: str = "N/A"
    mid: str = "N/A"
    alias: str = "N/A"
    document_type: str = "N/A"
    document_number: str = "N/A"
    name: str = "N/A"
    blocked: bool = False
    test_mode: bool = False
    accept_international_card: bool = True
    notification_url: str = "N/A"
    return_url: str = "N/A"
    status_change_url: str = "N/A"
    is_3ds_enabled: bool = False
    amex_mid: str = "N/A"
    facial_auth_enabled: bool = False

    def __init__(self):
        pass

    def write_to_json_file(self):
        data = {
            "ec": self.ec,
            "mid": self.mid,
            "alias": self.alias,
            "document_type": self.document_type,
            "document_number": self.document_number,
            "name": self.name,
            "blocked": self.blocked,
            "test_mode": self.test_mode,
            "accept_international_card": self.accept_international_card,
            "notification_url": self.notification_url,
            "return_url": self.return_url,
            "status_change_url": self.status_change_url,
            "is_3ds_enabled": self.is_3ds_enabled,
            "amex_mid": self.amex_mid,
            "facial_auth_enabled": self.facial_auth_enabled
        }

        file_path = f"{self.ec}.json"

        with open(file_path, "w") as json_file:
            json.dump(data, json_file, indent=4)