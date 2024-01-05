Attribute VB_Name = "CleanUpModule"
Sub CleanUpDraft()
Attribute CleanUpDraft.VB_ProcData.VB_Invoke_Func = " \n14"
    
    If ActiveWorkbook.Name = Globals.wbDraft() And _
       (ActiveSheet.Name = "2 (4 BankM)" Or ActiveSheet.Name = "2 (4 BankM V)" Or ActiveSheet.Name = "3 (4 BankS)") Then
        Call CleanUpSheet
    Else
        MsgBox ("Invalid workbook or sheet: " + ActiveWorkbook.Name + " [" + ActiveSheet.Name + "]")
    End If
    
End Sub

Sub CleanUpSheet()

    lastRow = ActiveSheet.Cells.SpecialCells(xlCellTypeLastCell).Row
    
    If lastRow <= 5 Or Range("L" + CStr(lastRow)).value = "" Then Exit Sub
   
    Range("L" + CStr(lastRow)).Select
    Selection.Copy
    Selection.PasteSpecial Paste:=xlPasteValues, Operation:=xlNone, SkipBlanks:=False, Transpose:=False
    
    Range("K" + CStr(lastRow)).Select
    Selection.PasteSpecial Paste:=xlPasteValues, Operation:=xlNone, SkipBlanks:=False, Transpose:=False
    
    Range("K4").Select
    Selection.PasteSpecial Paste:=xlPasteValues, Operation:=xlNone, SkipBlanks:=False, Transpose:=False
    
    Rows("5:" + CStr(lastRow - 1)).Select
    Selection.Delete Shift:=xlUp
    
    Range("K4").Select

End Sub

