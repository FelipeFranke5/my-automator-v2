import sys

class MissingRequiredArgs(Exception):
    pass

class InvalidUsernameLength(Exception):
    pass

class InvalidPasswordLength(Exception):
    pass

class InvalidEcLength(Exception):
    pass

class InvalidEc(Exception):
    pass

class SystemArguments:
    username: str
    password: str
    ec: str
    system_args: list[str]

    def __init__(self):
        try:
            self.system_args = sys.argv
            self.username = self.system_args[1]
            self.password = self.system_args[2]
            self.ec = self.system_args[3]
            self.validate_fields()
        except IndexError:
            raise MissingRequiredArgs()

    def validate_username(self):
        if len(self.username) < 5 or len(self.username) > 10:
            raise InvalidUsernameLength()

    def validate_password(self):
        if len(self.password) < 5 or len(self.password) > 20:
            raise InvalidPasswordLength()

    def validate_ec(self):
        if len(self.ec) != 10:
            raise InvalidEcLength()

        if not self.ec.isnumeric():
            raise InvalidEc()

    def validate_fields(self):
        self.validate_username()
        self.validate_password()
        self.validate_ec()