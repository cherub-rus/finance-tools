Attribute VB_Name = "CleanUpModule"
Sub CleanUpDraft()
Attribute CleanUpDraft.VB_ProcData.VB_Invoke_Func = " \n14"
    
    If ActiveWorkbook.Name = BOOK_DRAFT And _
       (ActiveSheet.Name Like "4 Bank*" Or ActiveSheet.Name Like "5 Марина *" Or ActiveSheet.Name Like "7 Марина *") Then
        Call CleanUpSheet
    Else
        MsgBox ("Invalid workbook or sheet: " + ActiveWorkbook.Name + " [" + ActiveSheet.Name + "]")
    End If
    
End Sub

Function CleanUpSheet()

    lastRow = ActiveSheet.Cells.SpecialCells(xlCellTypeLastCell).Row
    
    If lastRow <= 5 Or Range("L" + CStr(lastRow)).value = "" Then Exit Function
   
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

End Function

