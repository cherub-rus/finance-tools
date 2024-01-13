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

Public Const hc_comment = 2
Public Const hc_payee = 4
Public Const hc_category = 5
Public Const hc_message = 13

Public Const TRANS_RANGE = "$A$3:$M$5000"

Function wsPayee() As String
    wsPayee = "получатели"
End Function

Function wsHistory() As String
    wsHistory = "TransHistory"
End Function

Function wbDraft() As String
    wbDraft = "my_draft.xlsx"
End Function

Function wbHistory() As String
    wbHistory = "my_history.xlsx"
End Function

Function backupToGitPath() As String
    backupToGitPath = "..\finance-tools\vba_macros"
End Function

