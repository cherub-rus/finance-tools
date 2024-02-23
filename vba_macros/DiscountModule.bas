Attribute VB_Name = "DiscountModule"

Sub FixDiscountActiveSheet()
    FixDiscount (ActiveSheet.Name)
End Sub

Sub FixDiscount(sheetName As String)

    Dim ws As Worksheet

    Set ws = Workbooks(BOOK_DRAFT).Worksheets(sheetName)
    Call ClearWsFilter(ws)

    accountName$ = GetAccount(ws)

    Set lastCell = ws.Cells.SpecialCells(xlCellTypeLastCell)

    For iNum& = 4 To lastCell.Row
        dscDate$ = ws.Cells(iNum, c_date).Text
        dscTime$ = ws.Cells(iNum, c_time).Text
        Set dscAmmountCell = ws.Cells(iNum, c_amount)
        Set dscMarkCell = ws.Cells(iNum, c_mark)
        Set dscOperationCell = ws.Cells(iNum, c_operation)

        If dscOperationCell.Text <> "~Зачисление скидки" Or dscMarkCell.Text <> "" Then GoTo Continue

        trDate$ = ws.Cells(iNum + 1, c_date).Text
        trTime$ = ws.Cells(iNum + 1, c_time).Text
        Set trAmmountCell = ws.Cells(iNum + 1, c_amount)

        If trDate <> dscDate Or trTime <> dscTime Then
            dscOperationCell.Interior.Color = RGB(245, 157, 232)
            GoTo Continue
        End If

        ws.Cells(iNum + 1, c_amount_fee).value = 0 - dscAmmountCell.value
        ws.Cells(iNum + 1, c_amount_abs).value = 0 - trAmmountCell.value
        trAmmountCell.value = trAmmountCell.value + dscAmmountCell.value

        ws.Cells(iNum, c_amount_fee).value = dscAmmountCell.value
        ws.Cells(iNum, c_category).value = "#discount#"
        dscMarkCell.value = "xD"
        dscAmmountCell.value = ""

        iNum = iNum + 1
Continue:
    Next iNum

    Set sheetRange = ws.Range(Cells(4, 1).Address, lastCell.Address)
    sheetRange.AutoFilter Field:=c_mark, Criteria1:="<>xD"

End Sub



