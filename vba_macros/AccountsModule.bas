Attribute VB_Name = "AccountsModule"

Sub Export()

    Dim accountsData As Variant, outputPrefix As String
    accountsData = LoadAccountsData()

    Workbooks(BOOK_DRAFT).Activate

    outputPrefix = ActiveWorkbook.path & "\" & OWNER & "\" & "my_" & Format(Now(), "yyyy-mm-dd") '-hhmmss")

    Call CleanOutput(outputPrefix)

    For iNum = 1 To UBound(accountsData, 1)
        Dim aAccount As String, aType As String, aCard As String, aOrder As String, aSheet As String

        aAccount = accountsData(iNum, ac_account)
        aType = accountsData(iNum, ac_type)
        aCard = accountsData(iNum, ac_card)
        aOrder = accountsData(iNum, ac_order)
        aSheet = accountsData(iNum, ac_sheet)

        If aAccount <> "" Then
            If aSheet = "" Then
                Call UpdateBalances(aAccount)
                'TODO backup
                Call ExportSheet(outputPrefix, aAccount, aType, aCard)
                Call CleanUpSheet(aAccount)
            End If

            If aSheet = "Percents" Then
                'Debug.Print aAccount & " " & aAype & " " & a_order
                'TODO export
                'TODO cleanup amounts and rollback marks.
            End If
        End If
    Next iNum

End Sub

Sub CleanOutput(filePrefix As String)

    With CreateObject("Scripting.FileSystemObject")
        If .FileExists(filePrefix & ".csv") Then
            .DeleteFile filePrefix & ".csv"
        End If
        If .FileExists(filePrefix & ".qif") Then
            .DeleteFile filePrefix & ".qif"
        End If
    End With

End Sub

Function LoadAccountsData() As Variant

    Set ws = Workbooks(BOOK_DRAFT).Worksheets("Accounts")

    With ws.AutoFilter.Sort
         .SortFields.Clear
         .SortFields.Add Key:=Columns(ac_order), Order:=xlAscending
         .Apply
    End With

    Set lastCell = ws.Cells.SpecialCells(xlCellTypeLastCell)
    Set sheetRange = ws.Range(Cells(4, 1).Address, lastCell.Address)

    LoadAccountsData = sheetRange

    With ws.AutoFilter.Sort
         .SortFields.Clear
         .SortFields.Add Key:=Columns(ac_type), Order:=xlAscending
         .SortFields.Add Key:=Columns(ac_account), Order:=xlAscending
         .Apply
    End With

End Function

Sub CleanUpSheet(sheetName As String)

    Set ws = Workbooks(BOOK_DRAFT).Worksheets(sheetName)
    ws.Activate
    ws.AutoFilterMode = False

    lastRow = ws.Cells.SpecialCells(xlCellTypeLastCell).Row

    If Cells(lastRow, c_date).value = "#" Then
        lastRow = lastRow - 1
    End If

    If lastRow > 5 Then
        Range(Cells(5, 1), Cells(lastRow - 1, 1)).EntireRow.Select
        Selection.Delete Shift:=xlUp
    End If

    Cells(lastRow, c_balance).Select

End Sub


