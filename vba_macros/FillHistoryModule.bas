Attribute VB_Name = "FillHistoryModule"

Private Sub SortHistory()

    Set ws = Workbooks(BOOK_HISTORY).Worksheets(WS_HISTORY)
    ws.AutoFilter.Sort.SortFields.Clear

    Set firstCell = Cells(4, 1)
    Set lastCell = ws.Cells.SpecialCells(xlCellTypeLastCell)

    With ws.Sort
         .SortFields.Add Key:=Columns(hc_category), Order:=xlAscending
         .SortFields.Add Key:=Columns(hc_payee), Order:=xlAscending
         .SortFields.Add Key:=Columns(hc_message), Order:=xlAscending
         .SortFields.Add Key:=Columns(hc_comment), Order:=xlAscending
         .SetRange Range(firstCell.Address, lastCell.Address)
         .Header = xlNo
         .Apply
    End With

    ws.Range(firstCell.Address, lastCell.Address).RemoveDuplicates Columns:=Array(1, hc_payee, hc_category, hc_comment, hc_message), Header:=xlNo

    firstCell.Select

End Sub

Private Sub FillHistory()

    If ActiveSheet.Name = WS_HISTORY Then Exit Sub

    Dim lastCell As Range, sheetRange As Range, filterRange As Range, rowRange As Range, fillData As Variant, historyData As Variant

    fillData = LoadAutoFillData()
    historyData = LoadHistoryData()

    ActiveSheet.AutoFilterMode = False

    Set lastCell = ActiveSheet.Cells.SpecialCells(xlCellTypeLastCell)
    Set sheetRange = ActiveSheet.Range(Cells(2, 1).Address, lastCell.Address)

    With sheetRange
        .AutoFilter Field:=1, Criteria1:="<>"

        Set filterRange = .SpecialCells(xlCellTypeVisible).EntireRow

        For Each rowRange In filterRange
            Dim trDate As String

            trDate = rowRange.Cells(1, c_date).value

            Select Case trDate
            Case "#", ""
                'Skip
            Case "Account"
                'Skip
            Case Else
                If Not IsEmpty(rowRange.Cells(1, c_message)) And Not rowRange.Cells(1, c_mark) = "*" Then
                   added = FindOrAddHistoryRow(historyData, fillData, rowRange)
                   If added Then
                       historyData = LoadHistoryData()
                   End If
                End If
            End Select
        Next
    End With

    ActiveSheet.AutoFilterMode = False
End Sub

Function FindOrAddHistoryRow(historyData As Variant, fillData As Variant, rowRange As Range) As Boolean

    Dim iNum As Integer, newRowRange As Range

    trComment = rowRange.Cells(1, c_comment).value
    trPayee = rowRange.Cells(1, c_payee).value
    trCategory = rowRange.Cells(1, c_category).value
    trMessageSource = rowRange.Cells(1, c_message).value

    'Debug.Print "PROCESSING:" + trMessageSource

    If LCase(hdMessageSource) Like "*cashback*" Then
        FindOrAddHistoryRow = False
        GoTo ReturnFun
    End If

    If rowRange.Cells(1, c_mark).value = "*" Or trComment Like "*:*" Or trComment Like "*;*" Then 'Or trComment Like "#*" Then
        trComment = ""
    End If

    For iNum = 1 To UBound(historyData, 1)
        hdComment = historyData(iNum, hc_comment)
        hdPayee = historyData(iNum, hc_payee)
        hdCategory = historyData(iNum, hc_category)
        hdMessageSource = historyData(iNum, hc_message)

        If ((LCase(trComment) = LCase(hdComment)) And _
            (LCase(trPayee) = LCase(hdPayee)) And _
            (LCase(trCategory) = LCase(hdCategory)) And _
            (LCase(trMessageSource) = LCase(hdMessageSource))) Then
            FindOrAddHistoryRow = False
            GoTo ReturnFun
        End If
    Next iNum

    For iNum = 1 To UBound(fillData, 1)
        fdMask = fillData(iNum, 1)
        fdPayee = fillData(iNum, 2)
        fdCategory = fillData(iNum, 3)

        If (LCase(trMessageSource) Like LCase(fdMask)) And (LCase(trPayee) = LCase(fdPayee)) And (LCase(trCategory) = LCase(fdCategory)) Then
            FindOrAddHistoryRow = False
            GoTo ReturnFun
        End If
    Next iNum

    'Debug.Print "NEW ROW FOR:" + trMessageSource
    Set newRowRange = AddHistoryRow()
    With newRowRange
        .Cells(1, 1).value = "+"
        .Cells(1, hc_comment).value = trComment
        .Cells(1, hc_payee).value = trPayee
        .Cells(1, hc_category).value = trCategory
        .Cells(1, hc_message).value = trMessageSource
    End With
    FindOrAddHistoryRow = True

ReturnFun:

End Function

Function LoadHistoryData() As Variant

    Set ws = Workbooks(BOOK_HISTORY).Worksheets(WS_HISTORY)

    Set lastCell = ws.Cells.SpecialCells(xlCellTypeLastCell)
    Set sheetRange = ws.Range(Cells(4, 1).Address, lastCell.Address)

    LoadHistoryData = sheetRange

End Function

Function LoadAutoFillData() As Variant

    Set ws = Workbooks(BOOK_HISTORY).Worksheets("AutoFill")

    Set lastCell = ws.Cells.SpecialCells(xlCellTypeLastCell)
    Set sheetRange = ws.Range(Cells(2, 1).Address, lastCell.Address)

    LoadAutoFillData = sheetRange
End Function

Function AddHistoryRow() As Range

    Set ws = Workbooks(BOOK_HISTORY).Worksheets(WS_HISTORY)

    lastRow = ws.Cells.SpecialCells(xlCellTypeLastCell).Row
    Set newRowRange = ws.Rows(lastRow + 1).Cells

    Set AddHistoryRow = newRowRange

End Function
