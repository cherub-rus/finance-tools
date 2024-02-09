Attribute VB_Name = "ReorderModule"
Sub ReorderColumns()
Attribute ReorderColumns.VB_ProcData.VB_Invoke_Func = " \n14"

    Columns("B:B").Cut
    Columns("G:G").Insert Shift:=xlToRight

    Columns("H:H").Cut
    Columns("B:B").Insert Shift:=xlToRight

    Columns("K:L").Cut
    Columns("H:H").Insert Shift:=xlToRight

    Columns("K:L").Cut
    Columns("J:J").Insert Shift:=xlToRight

    Columns("C:C").Insert Shift:=xlToRight

    Application.CutCopyMode = False

    Call ColorSheet(ActiveSheet.Name)

    Call FixTexts
    Call FixColors
    Call FixSizes
End Sub

Sub FixTexts()

    Range("B2").value = Range("H2").value
    Range("H2").value = ""
    Range("C2").value = Range("D2").value
    Range("D2").value = Range("E2").value
    Range("E2").value = ""

    Range("B4").value = Range("H4").value
    Range("H4").value = ""

End Sub

Sub FixColors()
    With Range("B2:C2").Interior
        .pattern = xlSolid
        .Color = 49407
    End With

    Range("E2:M2").Interior.pattern = xlNone
    Range("J4:M4").Interior.pattern = xlNone
    Range("J6:M6").Interior.pattern = xlNone
End Sub

Sub FixSizes()
Attribute FixSizes.VB_ProcData.VB_Invoke_Func = " \n14"
    Columns(1).ColumnWidth = 11
    Columns(2).ColumnWidth = 10
    Columns(3).ColumnWidth = 11
    Columns(4).ColumnWidth = 11
    Columns(5).ColumnWidth = 25
    Columns(6).ColumnWidth = 25
    Columns(7).ColumnWidth = 2.2
    Columns(8).ColumnWidth = 51
    Columns(9).ColumnWidth = 11
    Columns(10).ColumnWidth = 11
    Columns(11).ColumnWidth = 10
    Columns(12).ColumnWidth = 10
    Columns(13).ColumnWidth = 25
    Columns(14).ColumnWidth = 37
End Sub
