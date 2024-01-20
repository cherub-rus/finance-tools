Attribute VB_Name = "ExportModule"


Sub ExportSheet(flName As String, accountName As String, accountType As String, accountCard As String)

    csvContent = ""
    qifContent = ""

    csvHeader = MakeCsvAccountHeader(accountName, accountType, accountCard)
    qifHeader = MakeQifAccountHeader(accountName, accountType)

    Set ws = Workbooks(BOOK_DRAFT).Worksheets(accountName)
    ws.AutoFilterMode = False

    Set lastCell = ws.Cells.SpecialCells(xlCellTypeLastCell)
    Set sheetRows = ws.Range(Cells(4, 1).Address, lastCell.Address).EntireRow

    For Each trans In sheetRows
        trDate = trans.Cells(1, c_date).value
        trAmount = trans.Cells(1, c_amount).Text
        trPayee = trans.Cells(1, c_payee).value
        trCategory = trans.Cells(1, c_category).value
        trComment = trans.Cells(1, c_comment).value
        trMark = trans.Cells(1, c_mark).value

        If trDate = "" Or trMark Like "x*" Then GoTo nextTrans

        csvContent = csvContent & vbCrLf & _
            trDate & vbTab & _
            trComment & vbTab _
            & trAmount & vbTab _
            & trPayee & vbTab _
            & trCategory & vbTab _
            & trMark

        If trDate = "#" Then GoTo nextTrans

        qifContent = qifContent & vbCrLf & _
            "D" & Replace(trDate, ".", "/") & vbCrLf & _
            IIf(trMark = "*", "C*" & vbCrLf, "") & _
            IIf(trComment <> "", "M" & trComment & vbCrLf, "") & _
            "T" & trAmount & vbCrLf & _
            IIf(trPayee <> "", "P" & trPayee & vbCrLf, "") & _
            "L" & trCategory & vbCrLf _
            & "^"

nextTrans:
    Next

    If csvContent <> "" Then
        csvContent = csvHeader & csvContent
        Open flName & ".csv" For Append As #1
            Print #1, csvContent
        Close #1
    End If

    If qifContent <> "" Then
        qifContent = qifHeader & qifContent
        Open flName & ".qif" For Append As #2
            Print #2, qifContent
        Close #2
    End If

End Sub

Function MakeQifAccountHeader(accountName As String, accountType As String) As String
    MakeQifAccountHeader = _
        "!Account" & vbCrLf & _
        "N" & accountName & vbCrLf & _
        "T" & accountType & vbCrLf & _
        "^" & vbCrLf & _
        "!Type:" & accountType
End Function

Function MakeCsvAccountHeader(accountName As String, accountType As String, accountCard As String) As String
    MakeCsvAccountHeader = _
        "" & vbTab & vbTab & vbTab & vbTab & vbTab & vbCrLf & _
        "" & vbTab & vbTab & vbTab & vbTab & vbTab & vbCrLf & _
        "Account" & vbTab & accountCard & vbTab & accountType & vbTab & accountName & vbTab & vbTab & vbCrLf & _
        "" & vbTab & vbTab & vbTab & vbTab & vbTab '& vbCrLf
End Function

Sub UpdateBalances(sheetName As String)

    Set ws = Workbooks(BOOK_DRAFT).Worksheets(sheetName)
    ws.Activate
    ws.AutoFilterMode = False

    lastRow = ws.Cells.SpecialCells(xlCellTypeLastCell).Row
    footerRow = 0

    If Cells(lastRow, c_date).value = "#" Then
        footerRow = lastRow
        lastRow = lastRow - 1
    Else
        noFooter = True
        footerRow = lastRow + 1
    End If

    Set balanceTestCell = Cells(lastRow, c_balance_formula)
    Set balanceCell = Cells(lastRow, c_balance)

    If lastRow < 5 Or balanceTestCell.value = "" Then Exit Sub

    balanceTest = balanceTestCell.value
    balanceTestCell.value = balanceTest

    balance = balanceCell.value
    balanceCell.value = balance

    If noFooter Then
        Cells(footerRow, c_date).value = "#"
        Cells(footerRow, c_balance).value = balance
        Range(Cells(footerRow, c_date), Cells(footerRow, c_balance)).Interior.Color = 15773696
    End If

End Sub

