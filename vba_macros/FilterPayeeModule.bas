Attribute VB_Name = "FilterPayeeModule"

Private Sub AddPayeeShortkeys()
    'Application.OnKey "+^{�}", "FilterPayee"
    'Application.OnKey "+^{�}", "FilterPayeeByCategory"
    'Application.OnKey "+^{�}", "CopyPayee"
End Sub

Sub FilterPayee()
Attribute FilterPayee.VB_ProcData.VB_Invoke_Func = "�\n14"
    'Ctrl Shift + R [�]
    Dim pattern, searchString As String

    searchString = InputBox("������� ������:", "����� ����������", ActiveCell.value)
    If searchString = "" Then Exit Sub

    pattern = "*" + searchString + "*"

    Workbooks(BOOK_HISTORY).Activate
    Worksheets(WS_PAYEE).Activate

    ActiveSheet.Range("$B$3:$C$500").AutoFilter Field:=2
    ActiveSheet.Range("$B$3:$C$500").AutoFilter Field:=1, Criteria1:=pattern
    ActiveWindow.ScrollRow = 1
    ActiveSheet.Cells(3, 2).Select
End Sub

Sub FilterPayeeByCategory()
Attribute FilterPayeeByCategory.VB_ProcData.VB_Invoke_Func = "�\n14"
    'Ctrl Shift + W [�]
    Dim pattern, searchString As String

    searchString = InputBox("������� ������:", "����� ���������� �� ���������")
    If searchString = "" Then Exit Sub

    pattern = "*" + searchString + "*"

    Workbooks(BOOK_HISTORY).Activate
    Worksheets(WS_PAYEE).Activate

    ActiveSheet.Range("$B$3:$C$500").AutoFilter Field:=1
    ActiveSheet.Range("$B$3:$C$500").AutoFilter Field:=2, Criteria1:=pattern
    ActiveWindow.ScrollRow = 1
    ActiveSheet.Cells(3, 2).Select
End Sub

Sub CopyPayee()
Attribute CopyPayee.VB_ProcData.VB_Invoke_Func = "�\n14"
    'Ctrl Shift + M [�]
    If ActiveSheet.Name <> WS_PAYEE Then Exit Sub

    ActiveSheet.Range(ActiveCell, ActiveCell.End(xlToRight)).Copy

    Windows(BOOK_DRAFT).Activate
    ActiveSheet.Paste
End Sub

