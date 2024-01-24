Attribute VB_Name = "ColorMyFinancesModule"

Sub ColorMyFinances()
Attribute ColorMyFinances.VB_ProcData.VB_Invoke_Func = "С\n14"
    'Ctrl Shift + C

    Response = MsgBox("Правим условное форматирование для " + vbCrLf + "[" + ActiveSheet.Name + "]?", vbYesNo + vbCritical + vbDefaultButton2, "Уверен?")
    If Response = vbNo Then Exit Sub

    ColorSheet (ActiveSheet.Name)
End Sub

Sub ColorSheet(sheetName As String)

    Set ws = ActiveWorkbook.Worksheets(sheetName)
    ws.Cells.FormatConditions.Delete

    Set fc_C1 = ws.Columns(c_date).FormatConditions
    With fc_C1.Add(Type:=xlExpression, Formula1:="=И(НЕ(ЕПУСТО(RC));НЕ(RC=""#"");НЕ(RC=""Account"");НЕ(ЛЕВСИМВ(RC6;1)=""x""))")
        .Interior.Color = RGB(255, 255, 0)
        .StopIfTrue = False
    End With

    Set fc_C3 = ws.Columns(c_amount).FormatConditions
    With fc_C3.Add(Type:=xlExpression, Formula1:="=И(ЕЧИСЛО(RC3);ЕПУСТО(RC5))")
        .Interior.Color = RGB(242, 242, 242)
        .Font.Color = RGB(255, 0, 0)
        .StopIfTrue = False
    End With

    Set fc_C5 = ws.Columns(c_category).FormatConditions
    With fc_C5.Add(Type:=xlExpression, Formula1:="=И(НЕ(ЕПУСТО(RC3));НЕ(RC1=""Account"");ЕПУСТО(RC))")
        .Interior.Color = RGB(250, 191, 143)
        .StopIfTrue = False
    End With

    Set fc_C6 = ws.Columns(c_mark).FormatConditions
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
    With fc_C6.Add(Type:=xlCellValue, Operator:=xlEqual, Formula1:="СП")
        .Interior.Color = RGB(146, 208, 80)
        .StopIfTrue = False
    End With

    Set fc_C12 = ws.Columns(c_balance_formula).FormatConditions
    With fc_C12.Add(Type:=xlCellValue, Operator:=xlNotEqual, Formula1:="=RC[-1]")
        .Font.Color = RGB(255, 0, 0)
        .StopIfTrue = False
    End With
    With fc_C12.Add(Type:=xlCellValue, Operator:=xlLess, Formula1:="0")
        .Interior.Color = RGB(245, 157, 232)
        .StopIfTrue = False
    End With

End Sub
