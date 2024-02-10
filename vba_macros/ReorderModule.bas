Attribute VB_Name = "ReorderModule"

Sub ReorderColumns()

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

Private Sub FixTexts()

    Range("B2").value = Range("H2").value
    Range("H2").value = ""
    Range("C2").value = Range("D2").value
    Range("D2").value = Range("E2").value
    Range("E2").value = ""

    Range("B4").value = Range("H4").value
    Range("H4").value = ""

End Sub

Private Sub FixColors()

    With Range("B2:C2").Interior
        .pattern = xlSolid
        .Color = 49407
    End With

    Range("E2:M2").Interior.pattern = xlNone
    Range("J4:M4").Interior.pattern = xlNone
    Range("J6:M6").Interior.pattern = xlNone

End Sub

Private Sub FixSizes()

    Columns(c_date).ColumnWidth = 11
    Columns(c_time).ColumnWidth = 10
    Columns(c_account).ColumnWidth = 11
    Columns(c_amount).ColumnWidth = 11
    Columns(c_payee).ColumnWidth = 25
    Columns(c_category).ColumnWidth = 25
    Columns(c_mark).ColumnWidth = 2.2
    Columns(c_comment).ColumnWidth = 51
    Columns(c_balance).ColumnWidth = 11
    Columns(c_balance_formula).ColumnWidth = 11
    Columns(c_amount_fee).ColumnWidth = 10
    Columns(c_amount_abs).ColumnWidth = 10
    Columns(c_operation).ColumnWidth = 25
    Columns(c_message).ColumnWidth = 37

End Sub
