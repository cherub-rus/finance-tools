Attribute VB_Name = "AccountsModule"

Sub Export()

    Dim accountsData As Variant
    accountsData = LoadAccountsData()

    Workbooks(BOOK_DRAFT).Activate

    Set fso = CreateObject("Scripting.FileSystemObject")
    basePath$ = ActiveWorkbook.path & "\" & OWNER & "\"
    awFileName$ = fso.GetBaseName(ActiveWorkbook.Name)
    awFileExt$ = fso.GetExtensionName(ActiveWorkbook.Name)
    stringNow$ = Format(Now(), "yyyy-mm-dd-hhmmss")

    backupName$ = basePath & awFileName & "_" & stringNow & "." & awFileExt
    ActiveWorkbook.SaveCopyAs backupName

    outputPrefix$ = basePath & "my_" & stringNow
    Call CleanOutput(outputPrefix)

    For iNum& = 1 To UBound(accountsData, 1)

        aAccount$ = accountsData(iNum, ac_account)
        aType$ = accountsData(iNum, ac_type)
        aCard$ = accountsData(iNum, ac_card)
        aOrder% = accountsData(iNum, ac_order)
        aSheet$ = accountsData(iNum, ac_sheet)

        If aAccount <> "" Then
            If aSheet = "" Then
                balance@ = UpdateBalances(aAccount)
                Call SetAccountBalance(aAccount, balance)
                Call ExportAccount(outputPrefix, aAccount, aAccount, aType, aCard)
                Call CleanUpSheet(aAccount)
            End If

            If aSheet = WS_PERCENTS Then
                Call ExportAccount(outputPrefix, WS_PERCENTS, aAccount, aType, aCard)
            End If
        End If
    Next iNum
    Call CleanUpPercents

End Sub

Sub CleanOutput(filePrefix As String)

    With CreateObject("Scripting.FileSystemObject")
        If .FileExists(filePrefix & ".csv") Then
            .DeleteFile filePrefix & ".csv"
        End If
        If .FileExists(filePrefix & ".qif") Then
            .DeleteFile filePrefix & ".qif"
        End If
    End With

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
    
    Cells(3, 1).Select

End Function

Function UpdateBalances(sheetName As String) As Currency

    Dim ws As Worksheet
    Set ws = Workbooks(BOOK_DRAFT).Worksheets(sheetName)
    ws.Activate
    Call ClearWsFilter(ws)

    lastRow = ws.Cells.SpecialCells(xlCellTypeLastCell).Row
    footerRow = 0

    If Cells(lastRow, c_date).value = "#" Then
        footerRow = lastRow
        lastRow = lastRow - 1
    Else
        noFooter = True
        footerRow = lastRow + 1
    End If

    Set balanceTestCell = Cells(lastRow, c_balance_formula)
    Set balanceCell = Cells(lastRow, c_balance)

    If lastRow < 5 Or balanceTestCell.value = "" Then Exit Function

    balanceTest = balanceTestCell.value
    balanceTestCell.value = balanceTest

    balance = balanceCell.value
    balanceCell.value = balance

    If noFooter Then
        Cells(footerRow, c_date).value = "#"
        Cells(footerRow, c_balance).value = balance
        Range(Cells(footerRow, c_date), Cells(footerRow, c_balance)).Interior.Color = 15773696
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

Private Sub CleanUpSheet(sheetName As String)

    Dim ws As Worksheet
    Set ws = Workbooks(BOOK_DRAFT).Worksheets(sheetName)
    ws.Activate
    Call ClearWsFilter(ws)

    lastRow = ws.Cells.SpecialCells(xlCellTypeLastCell).Row

    If Cells(lastRow, c_date).value = "#" Then
        lastRow = lastRow - 1
    End If

    If lastRow > 5 Then
        Range(Cells(5, 1), Cells(lastRow - 1, 1)).EntireRow.Select
        Selection.Delete Shift:=xlUp
    End If

    Cells(3, 1).Select

End Sub

Private Sub CleanUpPercents()

    Dim ws As Worksheet
    Set ws = Workbooks(BOOK_DRAFT).Worksheets(WS_PERCENTS)
    ws.Activate
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

    Cells(3, 1).Select

End Sub
