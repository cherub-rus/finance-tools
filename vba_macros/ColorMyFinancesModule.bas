Attribute VB_Name = "ColorMyFinancesModule"

Sub ColorMyFinances()
Attribute ColorMyFinances.VB_ProcData.VB_Invoke_Func = "—\n14"
    'Ctrl Shift + C

    Response = MsgBox("œ‡‚ËÏ ÛÒÎÓ‚ÌÓÂ ÙÓÏ‡ÚËÓ‚‡ÌËÂ ‰Îˇ " + vbCrLf + "[" + ActiveSheet.Name + "]?", vbYesNo + vbCritical + vbDefaultButton2, "”‚ÂÂÌ?")
    If Response = vbNo Then
       Exit Sub
    End If

    With ActiveSheet.Cells.FormatConditions
        .Delete
    End With

    Dim fc_C1, fc_C3, fc_C5, fc_C6, fc_C12 As FormatConditions

    Set fc_C1 = ActiveSheet.Columns(c_date).FormatConditions

    With fc_C1.Add(Type:=xlExpression, Formula1:="=»(Õ≈(≈œ”—“Œ(RC));Õ≈(RC=""#"");Õ≈(RC=""Account"");Õ≈(À≈¬—»Ã¬(RC14;1)=""v""))")
    .Interior.Color = RGB(255, 255, 0)
    .StopIfTrue = False
    End With

    Set fc_C3 = ActiveSheet.Columns(c_amount).FormatConditions

    With fc_C3.Add(Type:=xlExpression, Formula1:="=»(≈◊»—ÀŒ(RC3);≈œ”—“Œ(RC5))")
    .Interior.Color = RGB(242, 242, 242)
    .Font.Color = RGB(255, 0, 0)
    .StopIfTrue = False
    End With

    Set fc_C5 = ActiveSheet.Columns(c_category).FormatConditions

    With fc_C5.Add(Type:=xlExpression, Formula1:="=»(Õ≈(≈œ”—“Œ(RC3));Õ≈(RC1=""Account"");≈œ”—“Œ(RC))")
    .Interior.Color = RGB(250, 191, 143)
    .StopIfTrue = False
    End With

    Set fc_C6 = ActiveSheet.Columns(c_mark).FormatConditions

    With fc_C6.Add(Type:=xlCellValue, Operator:=xlEqual, Formula1:="?")
    .Interior.Color = RGB(255, 192, 0)
    .Font.Color = RGB(255, 255, 255)
    .StopIfTrue = False
    End With

    With fc_C6.Add(Type:=xlCellValue, Operator:=xlEqual, Formula1:="*")
    .Interior.Color = RGB(255, 0, 0)
    .StopIfTrue = False
    End With

    With fc_C6.Add(Type:=xlCellValue, Operator:=xlEqual, Formula1:="#")
    .Interior.Color = RGB(127, 255, 255)
    .StopIfTrue = False
    End With

    With fc_C6.Add(Type:=xlCellValue, Operator:=xlEqual, Formula1:="—œ")
    .Interior.Color = RGB(146, 208, 80)
    .StopIfTrue = False
    End With

    Set fc_C12 = ActiveSheet.Columns(c_balance_formula).FormatConditions

    With fc_C12.Add(Type:=xlCellValue, Operator:=xlNotEqual, Formula1:="=RC[-1]")
    .Font.Color = RGB(255, 0, 0)
    .StopIfTrue = False
    End With

    With fc_C12.Add(Type:=xlCellValue, Operator:=xlLess, Formula1:="0")
    .Interior.Color = RGB(245, 157, 232)
    .StopIfTrue = False
    End With


'    Set fc_C17 = ActiveSheet.Columns(17).FormatConditions

'    With fc_C17.Add(Type:=xlCellValue, Operator:=xlEqual, Formula1:="=RC[-14]")
'    .Font.Color = RGB(200, 200, 200)
'    .StopIfTrue = False
'    End With

End Sub

