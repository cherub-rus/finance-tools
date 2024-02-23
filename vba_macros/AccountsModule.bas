Attribute VB_Name = "AccountsModule"

Sub AllExport()

    Dim accountsData As Variant
    accountsData = LoadAccountsData()

    Workbooks(BOOK_DRAFT).Activate

    nameBase$ = ActiveWorkbook.path & "\" & OWNER & "\export\" & Format(Now(), "yyyymmdd-hhmmss") & "_"
    balanceFileName$ = nameBase & "остатки.txt"
    outputPrefix$ = nameBase & OWNER

    Call CleanOutput(nameBase)

    ActiveWorkbook.SaveCopyAs nameBase & ActiveWorkbook.Name

    For iNum& = 1 To UBound(accountsData, 1)

        aAccount$ = accountsData(iNum, ac_account)
        aType$ = accountsData(iNum, ac_type)
        aCard$ = accountsData(iNum, ac_card)
        aOrder% = accountsData(iNum, ac_order)
        aSheet$ = accountsData(iNum, ac_sheet)

        If aAccount <> "" Then
            If aSheet = "" Then
                Call ExportAccount(outputPrefix, aAccount, aAccount, aType, aCard)
                balance@ = UpdateBalances(aAccount)
                Call SetAccountBalance(aAccount, balance)
                Call FileAppend(balanceFileName, aAccount & vbTab & FormatCurrency(balance, 2, vbTrue, vbFalse, vbFalse))
            End If

            If aSheet = WS_PERCENTS Then
                Call ExportAccount(outputPrefix, WS_PERCENTS, aAccount, aType, aCard)
            End If
        End If
    Next iNum

    Call CleanUp(accountsData)

    ActiveWorkbook.Worksheets("Accounts").Activate
    Cells(3, 1).Select

End Sub

Private Sub CleanUp(accountsData As Variant)

    For iNum& = 1 To UBound(accountsData, 1)

        aAccount$ = accountsData(iNum, ac_account)
        aSheet$ = accountsData(iNum, ac_sheet)

        If aAccount <> "" And aSheet = "" Then
            Call CleanUpSheet(aAccount)
        End If

    Next iNum
    Call CleanUpPercents

End Sub

Sub CleanOutput(filePrefix As String)
    On Error Resume Next
    Kill filePrefix & "*.*"
End Sub

Function LoadAccountsData() As Variant

    Set ws = Workbooks(BOOK_DRAFT).Worksheets("Accounts")

    With ws.AutoFilter.Sort
         .SortFields.Clear
         .SortFields.Add Key:=Columns(ac_order), Order:=xlAscending
         .Apply
    End With

    Set lastCell = ws.Cells.SpecialCells(xlCellTypeLastCell)
    Set sheetRange = ws.Range(Cells(4, 1).Address, lastCell.Address)

    LoadAccountsData = sheetRange

    With ws.AutoFilter.Sort
         .SortFields.Clear
         .SortFields.Add Key:=Columns(ac_type), Order:=xlAscending
         .SortFields.Add Key:=Columns(ac_account), Order:=xlAscending
         .Apply
    End With

End Function

Function UpdateBalances(sheetName As String) As Currency

    Dim ws As Worksheet
    Set ws = Workbooks(BOOK_DRAFT).Worksheets(sheetName)
    Call ClearWsFilter(ws)

    lastRow = ws.Cells.SpecialCells(xlCellTypeLastCell).Row
    footerRow = 0

    If ws.Cells(lastRow, c_date).value = "#" Then
        footerRow = lastRow
        lastRow = lastRow - 1
    Else
        noFooter = True
        footerRow = lastRow + 1
    End If

    Set balanceTestCell = ws.Cells(lastRow, c_balance_formula)
    Set balanceCell = ws.Cells(lastRow, c_balance)

    If lastRow < 5 Or balanceTestCell.value = "" Then Exit Function

    balanceTest = balanceTestCell.value
    balanceTestCell.value = balanceTest

    balance = balanceCell.value
    balanceCell.value = balance

    ws.Cells(lastRow, c_mark).value = "x"

    If noFooter Then
        ws.Cells(footerRow, c_date).value = "#"
        ws.Cells(footerRow, c_balance).value = balance
        ws.Range(ws.Cells(footerRow, c_date), ws.Cells(footerRow, c_balance)).Interior.Color = 15773696
    End If

    UpdateBalances = balance
End Function

Sub SetAccountBalance(accountName As String, balance As Currency)

    Set ws = Workbooks(BOOK_DRAFT).Worksheets("Accounts")

    With ws.Columns(ac_account)
        If Not .Find(What:=accountName, LookIn:=xlValues) Is Nothing Then
            accountRow = .Find(What:=accountName, LookIn:=xlValues).Row
            ws.Cells(accountRow, ac_balance).value = balance
        End If
    End With

End Sub

Private Sub CleanUpActiveSheet()
    Call CleanUpSheet(ActiveSheet.Name)
End Sub

Private Sub CleanUpSheet(sheetName As String)

    Dim ws As Worksheet
    Set ws = Workbooks(BOOK_DRAFT).Worksheets(sheetName)
    Call ClearWsFilter(ws)

    lastRow = ws.Cells.SpecialCells(xlCellTypeLastCell).Row

    If ws.Cells(lastRow, c_date).value = "#" Then
        lastRow = lastRow - 1
    End If

    If lastRow > 5 Then
        ws.Range(ws.Cells(5, 1), ws.Cells(lastRow - 1, 1)).EntireRow.Delete
    End If

    Application.GoTo ws.Cells(1, 1), True

End Sub

Private Sub CleanUpPercents()

    Dim ws As Worksheet
    Set ws = Workbooks(BOOK_DRAFT).Worksheets(WS_PERCENTS)
    Call ClearWsFilter(ws)

    Set lastCell = ws.Cells.SpecialCells(xlCellTypeLastCell)
    Set sheetRows = ws.Range(Cells(5, 1).Address, lastCell.Address).EntireRow

    For Each trans In sheetRows
        If trans.Cells(1, c_amount).value <> "" Then
            trans.Cells(1, c_amount).value = ""
        End If

        If trans.Cells(1, c_mark).value = "" Then
            trans.Cells(1, c_mark).value = "x"
        End If
    Next

End Sub

Function GetAccount(ws As Worksheet) As String
    GetAccount = ws.Cells(2, 4)
End Function

