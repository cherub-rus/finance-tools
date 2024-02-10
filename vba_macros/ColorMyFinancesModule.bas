Attribute VB_Name = "ColorMyFinancesModule"

Sub ColorMyFinances()
Attribute ColorMyFinances.VB_ProcData.VB_Invoke_Func = "—\n14"
    'Ctrl Shift + C

    Response = MsgBox("œ‡‚ËÏ ÛÒÎÓ‚ÌÓÂ ÙÓÏ‡ÚËÓ‚‡ÌËÂ ‰Îˇ " + vbCrLf + "[" + ActiveSheet.Name + "]?", vbYesNo + vbCritical + vbDefaultButton2, "”‚ÂÂÌ?")
    If Response = vbNo Then Exit Sub

    ColorSheet (ActiveSheet.Name)
End Sub

Sub ColorSheet(sheetName As String)

    Set ws = ActiveWorkbook.Worksheets(sheetName)
    ws.Cells.FormatConditions.Delete

    Set fc_date = ws.Columns(c_date).FormatConditions
    With fc_date.Add(Type:=xlExpression, Formula1:="=»(Õ≈(≈œ”—“Œ(RC));Õ≈(RC=""#"");Õ≈(RC=""Account"");Õ≈(À≈¬—»Ã¬(RC8;1)=""x""))")
        .Interior.Color = RGB(255, 255, 0)
        .StopIfTrue = False
    End With

    Set fc_amount = ws.Columns(c_amount).FormatConditions
    With fc_amount.Add(Type:=xlExpression, Formula1:="=»(≈◊»—ÀŒ(RC4);≈œ”—“Œ(RC6))")
        .Interior.Color = RGB(242, 242, 242)
        .Font.Color = RGB(255, 0, 0)
        .StopIfTrue = False
    End With

    Set fc_category = ws.Columns(c_category).FormatConditions
    With fc_category.Add(Type:=xlExpression, Formula1:="=»(Õ≈(≈œ”—“Œ(RC4));Õ≈(RC1=""Account"");≈œ”—“Œ(RC))")
        .Interior.Color = RGB(250, 191, 143)
        .StopIfTrue = False
    End With

    Set fc_mark = ws.Columns(c_mark).FormatConditions
    With fc_mark.Add(Type:=xlCellValue, Operator:=xlEqual, Formula1:="?")
        .Interior.Color = RGB(255, 192, 0)
        .Font.Color = RGB(255, 255, 255)
        .StopIfTrue = False
    End With
    With fc_mark.Add(Type:=xlCellValue, Operator:=xlEqual, Formula1:="*")
        .Interior.Color = RGB(255, 0, 0)
        .StopIfTrue = False
    End With
    With fc_mark.Add(Type:=xlCellValue, Operator:=xlEqual, Formula1:="#")
        .Interior.Color = RGB(127, 255, 255)
        .StopIfTrue = False
    End With
    With fc_mark.Add(Type:=xlCellValue, Operator:=xlEqual, Formula1:="—œ")
        .Interior.Color = RGB(146, 208, 80)
        .StopIfTrue = False
    End With

    Set fc_bf = ws.Columns(c_balance_formula).FormatConditions
    With fc_bf.Add(Type:=xlCellValue, Operator:=xlNotEqual, Formula1:="=RC[-1]")
        .Font.Color = RGB(255, 0, 0)
        .StopIfTrue = False
    End With
    With fc_bf.Add(Type:=xlCellValue, Operator:=xlLess, Formula1:="0")
        .Interior.Color = RGB(245, 157, 232)
        .StopIfTrue = False
    End With

    Call FixSizes

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

