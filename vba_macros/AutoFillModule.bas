Attribute VB_Name = "AutoFillModule"

Sub FillActiveSheet()
    FillSheet (ActiveSheet.Name)
End Sub

Sub FillSheet(sheetName As String)

    Dim ws As Worksheet, lastCell As Range, sheetRange As Range, filterRange As Range, rowRange As Range

    fillData = LoadAutoFillData()

    Set ws = Workbooks(BOOK_DRAFT).Worksheets(sheetName)
    Call ClearWsFilter(ws)

    Set lastCell = ws.Cells.SpecialCells(xlCellTypeLastCell)
    Set sheetRange = ws.Range(Cells(2, 1).Address, lastCell.Address)

    With sheetRange
        .AutoFilter Field:=1, Criteria1:="<>"
        .AutoFilter Field:=5, Criteria1:="="

        Set filterRange = .SpecialCells(xlCellTypeVisible).EntireRow
        'TODO Function GetRangeToFill

        For Each rowRange In filterRange
            Dim trDate As String
            Dim accountName As String

            trDate = rowRange.Cells(1, c_date).value

            Select Case trDate
            Case "#", ""
                'Skip
            Case "Account"
                accountName = rowRange.Cells(1, c_payee).value
                'Debug.Print accountName
            Case Else
                If IsEmpty(rowRange.Cells(1, c_category)) Then
                   Call FillPayeeAndCategory(fillData, rowRange, accountName)
                End If
            End Select
        Next
    End With

    Call ClearWsFilter(ws)
End Sub

Private Sub FillPayeeAndCategory(fillData As Variant, rowRange As Range, accountName As String)

    trComment$ = rowRange.Cells(1, c_comment).value
    trMessage$ = rowRange.Cells(1, c_message).value
    trOperation$ = rowRange.Cells(1, c_operation).value

    For iNum = 1 To UBound(fillData, 1)

        fdAccount$ = fillData(iNum, fdc_account)
        If Len(fdAccount) > 0 And Not accountName Like fdAccount Then
            GoTo Continue
        End If

        fdOperationMask$ = fillData(iNum, fdc_operation)
        If Len(fdOperationMask) > 0 And Not LCase(trOperation) Like LCase(fdOperationMask) Then
            GoTo Continue
        End If

        fdMask$ = fillData(iNum, fdc_mask)
        If (LCase(trComment) Like LCase(fdMask)) Or (LCase(trMessage) Like LCase(fdMask)) Then
            With rowRange
                .Cells(1, c_payee).value = fillData(iNum, fdc_payee)
                .Cells(1, c_category).value = fillData(iNum, fdc_category)
                .Cells(1, c_comment).value = IIf(fillData(iNum, fdc_comment) <> "?", fillData(iNum, fdc_comment), trComment)
                .Cells(1, c_mark).value = IIf(fillData(iNum, fdc_mark) <> "", fillData(iNum, fdc_mark), "#")
                'Debug.Print .Cells(1, c_date).value & " " & .Cells(1, c_comment).value & " " & fillData(iNum, fdc_payee) & " " & fillData(iNum, fdc_category)
            End With
            GoTo Break
        End If
Continue:
    Next iNum
Break:

End Sub

Private Sub FillBankTransfer(rowRange As Range, account As String)
    With rowRange
        .Cells(1, c_category).value = account
        .Cells(1, c_comment).value = ""
        .Cells(1, c_mark).value = "#"
    End With
End Sub

Function LoadAutoFillData() As Variant

    Set ws = Workbooks(BOOK_HISTORY).Worksheets(WS_AUTOFILL)

    Set lastCell = ws.Cells.SpecialCells(xlCellTypeLastCell)
    Set sheetRange = ws.Range(Cells(2, 1).Address, lastCell.Address)

    LoadAutoFillData = sheetRange
End Function

