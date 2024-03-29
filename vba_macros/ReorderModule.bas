Attribute VB_Name = "ReorderModule"
Private Sub ReorderHead()

    Dim accountsData As Variant
    accountsData = LoadAccountsData()

    Workbooks(BOOK_DRAFT).Activate

    For iNum& = 1 To UBound(accountsData, 1)

        aAccount$ = accountsData(iNum, ac_account)
        aSheet$ = accountsData(iNum, ac_sheet)

        If aAccount <> "" And aSheet <> WS_PERCENTS Then

            Set ws = ActiveWorkbook.Worksheets(aAccount)

            ws.Range("E2").value = ws.Range("B2").value
            ws.Range("B2").value = ws.Range("C2").value
            ws.Range("C2").value = ws.Range("D2").value
            ws.Range("D2").value = ws.Range("E2").value
            ws.Range("E2").value = ""

            With ws.Range("B2:C2").Interior
                .pattern = xlSolid
                .Color = 49407
            End With

            ws.Range("E2").Interior.pattern = xlNone
        End If
    Next iNum

    ActiveWorkbook.Worksheets("Accounts").Activate
    Cells(3, 1).Select

End Sub

Private Sub ReorderColumns()

    Columns("B:B").Cut
    Columns("G:G").Insert Shift:=xlToRight

    Columns("H:H").Cut
    Columns("B:B").Insert Shift:=xlToRight

    Columns("K:L").Cut
    Columns("H:H").Insert Shift:=xlToRight

    Columns("K:L").Cut
    Columns("J:J").Insert Shift:=xlToRight

    Columns("C:C").Insert Shift:=xlToRight

    Columns(7).Cut
    Columns(9).Insert Shift:=xlToRight

    Application.CutCopyMode = False

    Call ColorSheet(ActiveSheet.Name)

    Call FixTexts
    Call FixColors
End Sub

Private Sub FixTexts()

    Range("B2").value = Range("G2").value
    Range("G2").value = ""
    Range("C2").value = Range("D2").value
    Range("D2").value = Range("E2").value
    Range("E2").value = ""

    Range("B4").value = Range("G4").value
    Range("G4").value = ""

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
