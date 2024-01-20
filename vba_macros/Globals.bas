Attribute VB_Name = "Globals"

Public Const c_date = 1
Public Const c_comment = 2
Public Const c_amount = 3
Public Const c_payee = 4
Public Const c_category = 5
Public Const c_mark = 6
Public Const c_operation = 7
Public Const c_time = 8
Public Const c_balance = 11
Public Const c_balance_formula = 12
Public Const c_message = 13

Public Const hc_comment = 4
Public Const hc_payee = 2
Public Const hc_category = 3
Public Const hc_message = 5

Public Const ac_account = 1
Public Const ac_type = 2
Public Const ac_card = 3
Public Const ac_order = 4
Public Const ac_sheet = 5

Public Const TRANS_RANGE = "A3:M5000"
Public Const PAYEE_RANGE = "B3:C500"

Public Const WS_PERCENTS = "Percents"
Public Const WS_PAYEE = "получатели"
Public Const WS_HISTORY = "TransHistory"

Public Const OWNER = "my"

Public Const BOOK_DRAFT = OWNER + "_draft.xlsx"
Public Const BOOK_HISTORY = OWNER + "_history.xlsx"

Public Const BASE_PATH = ".."
Public Const BACKUP_TO_GIT_PATH = BASE_PATH + "\finance-tools\vba_macros"

