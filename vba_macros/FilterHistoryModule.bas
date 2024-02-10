Attribute VB_Name = "FilterHistoryModule"

Sub zClearFilter()
Attribute zClearFilter.VB_ProcData.VB_Invoke_Func = "�\n14"
    'Ctrl Shift + Q [�]

    Call ClearWsFilter(ActiveSheet)
End Sub

Sub ClearWsFilter(ws As Worksheet)

    If ws.AutoFilterMode Then
        If ws.FilterMode Then ws.ShowAllData
    End If

End Sub

Sub zClearFilterForHistoryComment()
Attribute zClearFilterForHistoryComment.VB_ProcData.VB_Invoke_Func = "�\n14"
    'Ctrl Shift + S [�]
    Windows(BOOK_DRAFT).Activate
    ActiveSheet.Range(TRANS_RANGE).AutoFilter Field:=c_message
    ActiveSheet.Cells(4, c_comment).Select
End Sub

Sub zFilterHistoryByComment()
Attribute zFilterHistoryByComment.VB_ProcData.VB_Invoke_Func = "�\n14"
    'Ctrl Shift + D [�]
    Dim pattern, searchString As String

    searchString = InputBox("������� ������:", "����� �� �����������", ActiveCell.value)
    If searchString = "" Then Exit Sub

    pattern = "*" + searchString + "*"

    Windows(BOOK_DRAFT).Activate
    ActiveSheet.Range(TRANS_RANGE).AutoFilter Field:=c_message, Criteria1:=pattern
    ActiveWindow.ScrollRow = 1
    ActiveSheet.Cells(3, c_payee).Select

    Windows(BOOK_HISTORY).Activate
    If ActiveSheet.Name = WS_PAYEE Then Exit Sub

    ActiveSheet.Range(TRANS_RANGE).AutoFilter Field:=hc_payee
    ActiveSheet.Range(TRANS_RANGE).AutoFilter Field:=hc_message, Criteria1:=pattern
    ActiveWindow.ScrollRow = 1
    ActiveSheet.Cells(3, hc_payee).Select
End Sub

Sub zFilterHistoryByPayee()
Attribute zFilterHistoryByPayee.VB_ProcData.VB_Invoke_Func = "�\n14"
    'Ctrl Shift + E [�]
    Dim pattern, searchString As String

    searchString = InputBox("������� ������:", "����� �� ����������", ActiveCell.value)
    If searchString = "" Then Exit Sub

    pattern = "*" + searchString + "*"

    Windows(BOOK_HISTORY).Activate
    If ActiveSheet.Name = WS_PAYEE Then Exit Sub

    ActiveSheet.Range(TRANS_RANGE).AutoFilter Field:=hc_message
    ActiveSheet.Range(TRANS_RANGE).AutoFilter Field:=hc_payee, Criteria1:=pattern
    ActiveWindow.ScrollRow = 1
    ActiveSheet.Cells(3, hc_payee).Select
End Sub

Sub zFilterByAmount()
Attribute zFilterByAmount.VB_ProcData.VB_Invoke_Func = "�\n14"
    'Ctrl Shift + T [�]
    Dim pattern1, pattern2, searchString As String
    Dim value As Double

    searchString = InputBox("������� ������:", "����� �� �����", ActiveCell.value)
    If searchString = "" Then Exit Sub

    value = CDbl("-" + Replace(searchString, ".", ","))
    pattern1 = Format(value, "0.00")
    pattern2 = Format(value * 1.01, "0.00")

    Windows(BOOK_DRAFT).Activate
    ActiveSheet.Range(TRANS_RANGE).AutoFilter Field:=c_amount, Criteria1:=Array(pattern1, pattern2), Operator:=xlFilterValues
    ActiveWindow.ScrollRow = 1
    ActiveSheet.Cells(3, c_payee).Select
End Sub

