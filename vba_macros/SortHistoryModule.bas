Attribute VB_Name = "SortHistoryModule"
Sub SortHistory()

    Set ws = Workbooks(Globals.wbHistory()).Worksheets(Globals.wsHistory())

    Range("A3").Select
    Range(Selection, ActiveCell.SpecialCells(xlLastCell)).Select
    ws.AutoFilter.Sort.SortFields.Clear

    Set lastCell = ActiveSheet.Cells.SpecialCells(xlCellTypeLastCell)

    ws.AutoFilter.Sort.SortFields.Add2 Key _
        :=Range("E:E"), SortOn:=xlSortOnValues, Order:=xlAscending, DataOption:=xlSortNormal
    ws.AutoFilter.Sort.SortFields.Add2 Key _
        :=Range("D:D"), SortOn:=xlSortOnValues, Order:=xlAscending, DataOption:=xlSortNormal
    ws.AutoFilter.Sort.SortFields.Add2 Key _
        :=Range("M:M"), SortOn:=xlSortOnValues, Order:=xlAscending, DataOption:=xlSortNormal
    ws.AutoFilter.Sort.SortFields.Add2 Key _
        :=Range("B:B"), SortOn:=xlSortOnValues, Order:=xlAscending, DataOption:=xlSortNormal

    With ws.AutoFilter.Sort
        .Header = xlYes
        .MatchCase = False
        .Orientation = xlTopToBottom
        .SortMethod = xlPinYin
        .Apply
    End With

    ActiveSheet.Range("$A$3:" + lastCell.Address).RemoveDuplicates Columns:=Array(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13), Header:=xlYes

    Range("A3").Select

End Sub
