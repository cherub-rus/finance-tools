Attribute VB_Name = "FilterPayeeModule"

Private Sub AddPayeeShortkeys()
    'Application.OnKey "+^{к}", "FilterPayee"
    'Application.OnKey "+^{ц}", "FilterPayeeByCategory"
    'Application.OnKey "+^{ь}", "CopyPayee"
End Sub

Sub FilterPayee()
Attribute FilterPayee.VB_ProcData.VB_Invoke_Func = "К\n14"
    'Ctrl Shift + R [К]
    Dim pattern, searchString As String

    searchString = InputBox("Введите строку:", "Поиск получателя", ActiveCell.value)
    If searchString = "" Then Exit Sub

    pattern = "*" + searchString + "*"

    Workbooks(Globals.wbHistory()).Activate
    Worksheets(Globals.wsPayee()).Activate

    ActiveSheet.Range("$B$3:$C$500").AutoFilter Field:=2
    ActiveSheet.Range("$B$3:$C$500").AutoFilter Field:=1, Criteria1:=pattern
    ActiveWindow.ScrollRow = 3
End Sub

Sub FilterPayeeByCategory()
Attribute FilterPayeeByCategory.VB_ProcData.VB_Invoke_Func = "Ц\n14"
    'Ctrl Shift + W [Ц]
    Dim pattern, searchString As String

    searchString = InputBox("Введите строку:", "Поиск получателя по категории")
    If searchString = "" Then Exit Sub

    pattern = "*" + searchString + "*"

    Workbooks(Globals.wbHistory()).Activate
    Worksheets(Globals.wsPayee()).Activate

    ActiveSheet.Range("$B$3:$C$500").AutoFilter Field:=1
    ActiveSheet.Range("$B$3:$C$500").AutoFilter Field:=2, Criteria1:=pattern
    ActiveWindow.ScrollRow = 3
End Sub

Sub CopyPayee()
Attribute CopyPayee.VB_ProcData.VB_Invoke_Func = "Ь\n14"
    'Ctrl Shift + M [Ь]
    If ActiveSheet.Name <> Globals.wsPayee() Then Exit Sub

    ActiveSheet.Range(ActiveCell, ActiveCell.End(xlToRight)).Copy

    Windows(Globals.wbDraft()).Activate
    ActiveSheet.Paste
End Sub

