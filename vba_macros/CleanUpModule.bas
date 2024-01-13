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

    lastRow = ws.Cells.SpecialCells(xlCellTypeLastCell).Row
    footerRow = 0

    If Range("A" + CStr(lastRow)).value = "#" Then
        footerRow = lastRow
        lastRow = lastRow - 1
    End If

    If lastRow < 5 Or Range("L" + CStr(lastRow)).value = "" Then Exit Sub

    balanceL = Range("L" + CStr(lastRow)).value
    Range("L" + CStr(lastRow)).value = balanceL

    balance = Range("K" + CStr(lastRow)).value
    Range("K" + CStr(lastRow)).value = balance

    If footerRow = 0 Then
        footerRow = lastRow + 1
        Range("A" + CStr(footerRow)).value = "#"
        Range("K" + CStr(footerRow)).value = balance
        Range("A" + CStr(footerRow) + ":K" + CStr(footerRow)).Interior.Color = 15773696
    End If

    If lastRow > 5 Then
        Rows("5:" + CStr(lastRow - 1)).Select
        Selection.Delete Shift:=xlUp
    End If

    Range("K" + CStr(lastRow + 1)).Select

End Sub

Function LoadAccountsData() As Variant

    Set ws = Workbooks(BOOK_DRAFT).Worksheets("Accounts")

    With ws.AutoFilter.Sort
         .SortFields.Clear
         .SortFields.Add Key:=Columns(4), Order:=xlAscending
         .Apply
    End With

    Set lastCell = ws.Cells.SpecialCells(xlCellTypeLastCell)
    Set sheetRange = ws.Range("A4:" + lastCell.Address)

    LoadAccountsData = sheetRange

    With ws.AutoFilter.Sort
         .SortFields.Clear
         .SortFields.Add Key:=Columns(1), Order:=xlAscending
         .Apply
    End With

End Function
