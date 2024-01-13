Attribute VB_Name = "FilterHistoryModule"

Sub ClearFilter()
Attribute ClearFilter.VB_ProcData.VB_Invoke_Func = "Й\n14"
    'Ctrl Shift + Q [Й]

    If ActiveSheet.AutoFilterMode Then
        If ActiveSheet.FilterMode Then ActiveSheet.ShowAllData
    End If

End Sub

Sub ClearFilterForHistoryComment()
Attribute ClearFilterForHistoryComment.VB_ProcData.VB_Invoke_Func = "Ы\n14"
    'Ctrl Shift + S [Ы]
    Windows(Globals.wbDraft()).Activate
    ActiveSheet.Range(TRANS_RANGE).AutoFilter Field:=c_message
    ActiveSheet.Cells(4, c_comment).Select
End Sub

Sub FilterHistoryByComment()
Attribute FilterHistoryByComment.VB_ProcData.VB_Invoke_Func = "В\n14"
    'Ctrl Shift + D [В]
    Dim pattern, searchString As String

    searchString = InputBox("Введите строку:", "Поиск по комментарию", ActiveCell.value)
    If searchString = "" Then Exit Sub

    pattern = "*" + searchString + "*"

    Windows(Globals.wbDraft()).Activate
    ActiveSheet.Range(TRANS_RANGE).AutoFilter Field:=c_message, Criteria1:=pattern
    ActiveWindow.ScrollRow = 1
    ActiveSheet.Cells(3, c_payee).Select

    Windows(Globals.wbHistory()).Activate
    If ActiveSheet.Name = Globals.wsPayee() Then Exit Sub

    ActiveSheet.Range(TRANS_RANGE).AutoFilter Field:=hc_payee
    ActiveSheet.Range(TRANS_RANGE).AutoFilter Field:=hc_message, Criteria1:=pattern
    ActiveWindow.ScrollRow = 1
    ActiveSheet.Cells(3, hc_payee).Select
End Sub

Sub FilterHistoryByPayee()
Attribute FilterHistoryByPayee.VB_ProcData.VB_Invoke_Func = "У\n14"
    'Ctrl Shift + E [У]
    Dim pattern, searchString As String

    searchString = InputBox("Введите строку:", "Поиск по получателю", ActiveCell.value)
    If searchString = "" Then Exit Sub

    pattern = "*" + searchString + "*"

    Windows(Globals.wbHistory()).Activate
    If ActiveSheet.Name = Globals.wsPayee() Then Exit Sub

    ActiveSheet.Range(TRANS_RANGE).AutoFilter Field:=hc_message
    ActiveSheet.Range(TRANS_RANGE).AutoFilter Field:=hc_payee, Criteria1:=pattern
    ActiveWindow.ScrollRow = 1
    ActiveSheet.Cells(3, hc_payee).Select
End Sub

Sub FilterByAmount()
Attribute FilterByAmount.VB_ProcData.VB_Invoke_Func = "Е\n14"
    'Ctrl Shift + T [Е]
    Dim pattern1, pattern2, searchString As String
    Dim value As Double

    searchString = InputBox("Введите строку:", "Поиск по сумме", ActiveCell.value)
    If searchString = "" Then Exit Sub

    value = CDbl("-" + Replace(searchString, ".", ","))
    pattern1 = Format(value, "0.00")
    pattern2 = Format(value * 1.01, "0.00")

    Windows(Globals.wbDraft()).Activate
    ActiveSheet.Range(TRANS_RANGE).AutoFilter Field:=c_amount, Criteria1:=Array(pattern1, pattern2), Operator:=xlFilterValues
    ActiveWindow.ScrollRow = 1
    ActiveSheet.Cells(3, c_payee).Select
End Sub

