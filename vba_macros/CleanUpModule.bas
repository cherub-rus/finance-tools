Attribute VB_Name = "CleanUpModule"

Private Sub CleanUpDraft()

    Dim accountsData As Variant
    accountsData = LoadAccountsData()

    Workbooks(BOOK_DRAFT).Activate

    For iNum = 1 To UBound(accountsData, 1)
        aAccount = accountsData(iNum, ac_account)
        aType = accountsData(iNum, ac_type)
        aOrder = accountsData(iNum, ac_order)
        aSheet = accountsData(iNum, ac_sheet)

        If aAccount <> "" Then
            If aSheet = "" Then
                'Debug.Print "[" & aAccount & "]"
                Call CleanUpSheet(CStr(aAccount))
            End If

            If aSheet = "Percents" Then
                'TODO cleanup amounts and rollback marks.
                Debug.Print aAccount & " " & aType & " " & aOrder
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

