Attribute VB_Name = "ImportModule"

Sub Import()

    Dim accountsData As Variant
    accountsData = LoadAccountsData()

    Workbooks(BOOK_DRAFT).Worksheets("Accounts").Activate
    Cells(3, 1).Select

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
    Call ClearWsFilter(ws)

    lastRow = ws.Cells.SpecialCells(xlCellTypeLastCell).Row

    iRow = lastRow + 1
    For Each cLine In Split(content, Chr(10))
        If cLine = "" Or Left(cLine, 1) = vbTab Or Left(cLine, 8) = "Account" & vbTab Then _
            GoTo nextLine
        With ws
            iCol = 1
            For Each cCell In Split(cLine, vbTab)
                Select Case iCol
                Case c_date
                    .Cells(iRow, iCol).FormulaR1C1 = cCell
                Case c_amount, c_amount_fee, c_amount_abs
                    .Cells(iRow, iCol).FormulaR1C1 = Replace(cCell, ",", ".")
                Case c_balance, c_balance_formula
                    .Cells(iRow, iCol).FormulaR1C1 = FixFormula(CStr(cCell))
                Case Else
                    .Cells(iRow, iCol).FormulaR1C1 = cCell
                End Select
                iCol = iCol + 1
            Next cCell
            If Left(cLine, 1) = "#" Then _
                ws.Range(ws.Cells(iRow, c_date), ws.Cells(iRow, c_balance)).Interior.Color = 15773696
        End With
        iRow = iRow + 1
nextLine:
    Next cLine

End Sub

Private Function FixFormula(cellValue As String) As String
    FixFormula = IIf(Left(cellValue, 7) = "=Œ –”√À", Replace(Replace(cellValue, ";", ","), "Œ –”√À", "ROUND"), cellValue)
End Function
