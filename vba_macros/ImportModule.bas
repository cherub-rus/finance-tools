Attribute VB_Name = "ImportModule"

Sub Import()

    Dim accountsData As Variant
    accountsData = LoadAccountsData()

    Workbooks(BOOK_DRAFT).Activate

    srcPath$ = ActiveWorkbook.path & "\" & OWNER & "\"

    For iNum& = 1 To UBound(accountsData, 1)

        aAccount$ = accountsData(iNum, ac_account)
        aSheet$ = accountsData(iNum, ac_sheet)

        content$ = ""
        If aAccount <> "" And aSheet = "" Then
            content = FileRead(srcPath & aAccount & ".pdf.csv")
            Call LoadSheet(aAccount, content)
        End If
    Next iNum

    ActiveWorkbook.Worksheets("Accounts").Activate
    Cells(3, 1).Select

End Sub

Function FileRead(fileName As String) As String

    Set fso = CreateObject("Scripting.FileSystemObject")
    If Not fso.FileExists(fileName) Then Exit Function

    With CreateObject("ADODB.Stream")
        .Type = 2: .Charset = "utf-8": .Open
        .LoadFromFile (fileName)
        FileRead = .ReadText
        .Close
    End With

End Function

Private Sub LoadSheet(sheetName As String, content As String)

    Dim ws As Worksheet
    Set ws = Workbooks(BOOK_DRAFT).Worksheets(sheetName)
    ws.Activate
    Call ClearWsFilter(ws)

    lastRow = ws.Cells.SpecialCells(xlCellTypeLastCell).Row

    intRow = lastRow + 1
    For Each cLine In Split(content, Chr(10))
        If cLine <> "" Then
            With ws
                .Cells(intRow, 1) = cLine
                .Cells(intRow, 1).TextToColumns Destination:=Cells(intRow, 1), DataType:=xlDelimited, Tab:=True, FieldInfo:=Array(Array(1, 4))
                FixFormula (.Cells(intRow, c_balance))
                FixFormula (.Cells(intRow, c_balance_formula))
            End With
        End If
        intRow = intRow + 1
    Next cLine

End Sub

Private Sub FixFormula(cell As Range)
    If Left(cell.Text, 7) = "=Œ –”√À" Then _
        cell.FormulaR1C1 = Replace(Replace(cell.Text, ";", ","), "Œ –”√À", "ROUND")
End Sub
