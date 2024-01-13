Attribute VB_Name = "CleanUpModule"
Private Sub CleanUpDraft()
Attribute CleanUpDraft.VB_ProcData.VB_Invoke_Func = " \n14"
    
    If ActiveWorkbook.Name = BOOK_DRAFT And _
       (ActiveSheet.Name Like "4 Bank*" Or ActiveSheet.Name Like "5 Марина *" Or ActiveSheet.Name Like "7 Марина *") Then
        Call CleanUpSheet
    Else
        MsgBox ("Invalid workbook or sheet: " + ActiveWorkbook.Name + " [" + ActiveSheet.Name + "]")
    End If
    
End Sub

Private Sub CleanUpSheet()

    lastRow = ActiveSheet.Cells.SpecialCells(xlCellTypeLastCell).Row
    footerRow = 0

    If Range("A" + CStr(lastRow)).value = "#" Then
        footerRow = lastRow
        lastRow = lastRow - 1
    End If

    If lastRow < 5 Or Range("L" + CStr(lastRow)).value = "" Then Exit Sub

    Range("L" + CStr(lastRow)).Select
    Selection.Copy
    Selection.PasteSpecial Paste:=xlPasteValues

    Range("K" + CStr(lastRow)).Select
    Selection.Copy
    Selection.PasteSpecial Paste:=xlPasteValues

    If footerRow = 0 Then
        footerRow = lastRow + 1
        Range("A" + CStr(footerRow)).value = "#"
        Range("A" + CStr(footerRow) + ":K" + CStr(footerRow)).Interior.Color = 15773696

        Range("K" + CStr(footerRow)).Select
        Selection.PasteSpecial Paste:=xlPasteValues
    End If

    If lastRow > 5 Then
        Rows("5:" + CStr(lastRow - 1)).Select
        Selection.Delete Shift:=xlUp
    End If

    Range("K" + CStr(lastRow + 1)).Select

End Sub

