Attribute VB_Name = "MoveToHistoryModule"

Sub FillHistory()

    If ActiveSheet.Name = Globals.wsHistory() Then Exit Sub

    Dim lastCell As Range, sheetRange As Range, filterRange As Range, rowRange As Range, historyData As Variant

    historyData = LoadHistoryData()

    ActiveSheet.AutoFilterMode = False

    Set lastCell = ActiveSheet.Cells.SpecialCells(xlCellTypeLastCell)
    Set sheetRange = ActiveSheet.Range("$A$2:" + lastCell.Address)

    With sheetRange
        .AutoFilter Field:=1, Criteria1:="<>"
        .AutoFilter Field:=5, Criteria1:="<>"

        Set filterRange = .SpecialCells(xlCellTypeVisible).EntireRow

        For Each rowRange In filterRange
            Dim trDate As String
            Dim accountName As String

            trDate = rowRange.Cells(1, 1).value

            Select Case trDate
            Case "#", ""
                'Skip
            Case "Account"
                accountName = rowRange.Cells(1, 4).value
                'Debug.Print accountName
            Case Else
                If Not IsEmpty(rowRange.Cells(1, 13)) And Not rowRange.Cells(1, 13) Like "*cashback*" And Not rowRange.Cells(1, 6) = "*" Then
                   'Debug.Print rowRange.Cells(1, 13)
                   added = FindOrAddHistoryRow(historyData, rowRange, accountName)
                   If added Then
                       historyData = LoadHistoryData()
                   End If
                End If
            End Select
        Next
    End With

    ActiveSheet.AutoFilterMode = False
End Sub

Function FindOrAddHistoryRow(historyData As Variant, rowRange As Range, account As String) As Boolean

    Dim iNum As Integer, newRowRange As Range

    trComment = rowRange.Cells(1, 2).value
    trPayee = rowRange.Cells(1, 4).value
    trCategory = rowRange.Cells(1, 5).value
    trOperation = rowRange.Cells(1, 7).value
    trMessageSource = rowRange.Cells(1, 13).value

    If trComment Like "G*:*" Or trComment Like "*;*" Then
        trComment = ""
    End If

    For iNum = 1 To UBound(historyData, 1)
        hdComment = historyData(iNum, 2)
        hdPayee = historyData(iNum, 4)
        hdCategory = historyData(iNum, 5)
        hdOperation = historyData(iNum, 7)
        hdMessageSource = historyData(iNum, 13)

        If ((LCase(trComment) = LCase(hdComment)) And (LCase(trPayee) = LCase(hdPayee)) And (LCase(trCategory) = LCase(hdCategory)) And (LCase(trMessageSource) = LCase(hdMessageSource))) Then
            'Debug.Print "FOUND:" + trMessageSource
            FindOrAddHistoryRow = False
            GoTo OutFor
        End If
    Next iNum

    'Debug.Print "NEW ROW FOR:" + trMessageSource
    Set newRowRange = AddHistoryRow()
    With newRowRange
        .Cells(1, 1).value = account
        .Cells(1, 2).value = trComment
        .Cells(1, 4).value = trPayee
        .Cells(1, 5).value = trCategory
        .Cells(1, 7).value = trOperation
        .Cells(1, 13).value = trMessageSource
        'Debug.Print .Cells(1, 1).value & " " & .Cells(1, 2).value & " " & historyData(iNum, 2) & " " & historyData(iNum, 3)
    End With
    FindOrAddHistoryRow = True

OutFor:

End Function

Function LoadHistoryData() As Variant

    Set ws = Workbooks(Globals.wbHistory()).Worksheets(Globals.wsHistory())

    Set lastCell = ws.Cells.SpecialCells(xlCellTypeLastCell)
    Set sheetRange = ws.Range("$A$4:" + lastCell.Address)

    LoadHistoryData = sheetRange

End Function

Function AddHistoryRow() As Range

    Set ws = Workbooks(Globals.wbHistory()).Worksheets(Globals.wsHistory())

    lastRow = ws.Cells.SpecialCells(xlCellTypeLastCell).Row
    Set newRowRange = ws.Rows(lastRow + 1).Cells

    Set AddHistoryRow = newRowRange

End Function


