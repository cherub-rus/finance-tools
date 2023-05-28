Attribute VB_Name = "FilterHistoryModule"

Sub ClearFilter()
Attribute ClearFilter.VB_ProcData.VB_Invoke_Func = "�\n14"
    'Ctrl Shift + Q [�]
    If ActiveSheet.Name = Globals.wsPayee() Then
        ActiveSheet.Range("$B$3:$C$500").AutoFilter
        ActiveSheet.Range("$B$3:$C$500").AutoFilter
    Else
        ActiveSheet.Range("$A$3:$N$5000").AutoFilter
        ActiveSheet.Range("$A$3:$N$5000").AutoFilter
    End If
End Sub

Sub ClearFilterForHistoryComment()
Attribute ClearFilterForHistoryComment.VB_ProcData.VB_Invoke_Func = "�\n14"
    'Ctrl Shift + S [�]
    Windows(Globals.wbDraft()).Activate
    ActiveSheet.Range("$A$3:$N$5000").AutoFilter Field:=13
    ActiveSheet.Range("B4").Select
End Sub

Sub FilterHistoryByComment()
Attribute FilterHistoryByComment.VB_ProcData.VB_Invoke_Func = "�\n14"
    'Ctrl Shift + D [�]
    Dim pattern, searchString As String

    searchString = InputBox("������� ������:", "����� �� �����������", ActiveCell.value)
    If searchString = "" Then Exit Sub

    pattern = "*" + searchString + "*"

    Windows(Globals.wbDraft()).Activate
    ActiveSheet.Range("$A$3:$N$500").AutoFilter Field:=13, Criteria1:=pattern
    ActiveWindow.ScrollRow = 3
    ActiveSheet.Range("D2").Select

    Windows(Globals.wbHistory()).Activate
    If ActiveSheet.Name = Globals.wsPayee() Then Exit Sub

    ActiveSheet.Range("$A$3:$N$5000").AutoFilter Field:=4
    ActiveSheet.Range("$A$3:$N$5000").AutoFilter Field:=13, Criteria1:=pattern
    ActiveWindow.ScrollRow = 3
    ActiveSheet.Range("D2").Select
End Sub

Sub FilterHistoryByPayee()
Attribute FilterHistoryByPayee.VB_ProcData.VB_Invoke_Func = "�\n14"
    'Ctrl Shift + E [�]
    Dim pattern, searchString As String

    searchString = InputBox("������� ������:", "����� �� ����������", ActiveCell.value)
    If searchString = "" Then Exit Sub

    pattern = "*" + searchString + "*"

    Windows(Globals.wbHistory()).Activate
    If ActiveSheet.Name = Globals.wsPayee() Then Exit Sub

    ActiveSheet.Range("$A$3:$N$5000").AutoFilter Field:=13
    ActiveSheet.Range("$A$3:$N$5000").AutoFilter Field:=4, Criteria1:=pattern
    ActiveWindow.ScrollRow = 3
    ActiveSheet.Range("D2").Select
End Sub

Sub FilterByAmount()
Attribute FilterByAmount.VB_ProcData.VB_Invoke_Func = "�\n14"
    'Ctrl Shift + T [�]
    Dim pattern1, pattern2, searchString As String
    Dim value As Double

    searchString = InputBox("������� ������:", "����� �� �����", ActiveCell.value)
    If searchString = "" Then Exit Sub

    value = CDbl("-" + Replace(searchString, ".", ","))
    pattern1 = Format(value, "0.00")
    pattern2 = Format(value * 1.01, "0.00")

    Windows(Globals.wbDraft()).Activate
    ActiveSheet.Range("$A$3:$N$5000").AutoFilter Field:=3, Criteria1:=Array(pattern1, pattern2), Operator:=xlFilterValues
    ActiveWindow.ScrollRow = 4
    'ActiveSheet.Range("D3").Select
    'ActiveCell.Offset(1).Select
End Sub

