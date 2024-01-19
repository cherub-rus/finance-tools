Attribute VB_Name = "CleanUpModule"

Private Sub CleanUpDraft()

    Dim accountsData As Variant
    accountsData = LoadAccountsData()

    Workbooks(BOOK_DRAFT).Activate

    For iNum = 1 To UBound(accountsData, 1)
        ac_account = accountsData(iNum, 1)
        ac_type = accountsData(iNum, 2)
        ac_order = accountsData(iNum, 4)
        ac_sheet = accountsData(iNum, 5)

        If ac_account <> "" Then
            If ac_sheet = "" Then
                Debug.Print "[" & ac_account & "]"
                Call CleanUpSheet(CStr(ac_account))
            End If

            If ac_sheet = "Percent" Then
                Debug.Print ac_account & " " & ac_type & " " & ac_order
            End If
        End If

    Next iNum

End Sub

Private Sub CleanUpDraftManual()

    If ActiveWorkbook.Name = BOOK_DRAFT And _
       (ActiveSheet.Name Like "4 Bank*" Or ActiveSheet.Name Like "5 Марина *" Or ActiveSheet.Name Like "7 Марина *") Then
        Call CleanUpSheet(ActiveSheet.Name)
    Else
        MsgBox ("Invalid workbook or sheet: " + ActiveWorkbook.Name + " [" + ActiveSheet.Name + "]")
    End If

End Sub

Private Sub CleanUpSheet(sheetName As String)

    Set ws = Workbooks(BOOK_DRAFT).Worksheets(sheetName)
    ws.Activate
    ws.AutoFilterMode = False

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

    If lastRow < 5 Or balanceTestCell.value = "" Then Exit Sub

    balanceTest = balanceTestCell.value
    balanceTestCell.value = balanceTest

    balance = balanceCell.value
    balanceCell.value = balance

    If noFooter Then
        Cells(footerRow, c_date).value = "#"
        Cells(footerRow, c_balance).value = balance
        Range(Cells(footerRow, c_date), Cells(footerRow, c_balance)).Interior.Color = 15773696
    End If

    If lastRow > 5 Then
        Range(Cells(5, 1), Cells(lastRow - 1, 1)).EntireRow.Select
        Selection.Delete Shift:=xlUp
    End If

    balanceCell.Select

End Sub

Function LoadAccountsData() As Variant

    Set ws = Workbooks(BOOK_DRAFT).Worksheets("Accounts")

    With ws.AutoFilter.Sort
         .SortFields.Clear
         .SortFields.Add Key:=Columns(4), Order:=xlAscending
         .Apply
    End With

    Set lastCell = ws.Cells.SpecialCells(xlCellTypeLastCell)
    Set sheetRange = ws.Range(Cells(4, 1).Address, lastCell.Address)

    LoadAccountsData = sheetRange

    With ws.AutoFilter.Sort
         .SortFields.Clear
         .SortFields.Add Key:=Columns(2), Order:=xlAscending
         .SortFields.Add Key:=Columns(1), Order:=xlAscending
         .Apply
    End With

End Function
