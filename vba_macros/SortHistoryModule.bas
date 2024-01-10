Attribute VB_Name = "SortHistoryModule"

Sub SortHistory()

    Set ws = Workbooks(Globals.wbHistory()).Worksheets(Globals.wsHistory())

    Range("A4").Select
    Range(Selection, ActiveCell.SpecialCells(xlLastCell)).Select
    ws.AutoFilter.Sort.SortFields.Clear

    Set lastCell = ActiveSheet.Cells.SpecialCells(xlCellTypeLastCell)

    With ActiveSheet.Sort
         .SortFields.Add Key:=Columns(hc_category), Order:=xlAscending
         .SortFields.Add Key:=Columns(hc_payee), Order:=xlAscending
         .SortFields.Add Key:=Columns(hc_message), Order:=xlAscending
         .SortFields.Add Key:=Columns(hc_comment), Order:=xlAscending
         .SetRange Range("$A$4:" + lastCell.Address)
         .Header = xlNo
         .Apply
    End With

    ActiveSheet.Range("$A$3:" + lastCell.Address).RemoveDuplicates Columns:=Array(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13), Header:=xlNo

    Range("A4").Select

End Sub
