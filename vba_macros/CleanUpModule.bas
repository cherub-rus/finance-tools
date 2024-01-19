Attribute VB_Name = "CleanUpModule"

Private Sub CleanUpDraft()

    Dim accountsData As Variant
    accountsData = LoadAccountsData()

    Workbooks(BOOK_DRAFT).Activate

    For iNum = 1 To UBound(accountsData, 1)
        a_account = accountsData(iNum, ac_account)
        a_type = accountsData(iNum, ac_type)
        a_order = accountsData(iNum, ac_order)
        a_sheet = accountsData(iNum, ac_sheet)

        If a_account <> "" Then
            If a_sheet = "" Then
                Debug.Print "[" & a_account & "]"
                Call CleanUpSheet(CStr(a_account))
            End If

            If a_sheet = "Percents" Then
                'TODO
                Debug.Print a_account & " " & a_type & " " & a_order
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

