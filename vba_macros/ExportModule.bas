Attribute VB_Name = "ExportModule"

Sub ExportAccount(flName As String, sheetName As String, accountName As String, accountType As String, accountCard As String)

    Dim csvContent As String, qifContent As String

    csvHeader = MakeCsvAccountHeader(accountName, accountType, accountCard)
    qifHeader = MakeQifAccountHeader(accountName, accountType)

    Set ws = Workbooks(BOOK_DRAFT).Worksheets(sheetName)
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

        If sheetName = WS_PERCENTS Then
            trOperation = trans.Cells(1, c_operation).value
            If trOperation <> accountName Then GoTo nextTrans
        End If

        If trDate = "" Or (trDate = "#" And trComment = "") Or trMark Like "x*" Then GoTo nextTrans

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
        Call FileAppend(flName & ".csv", csvContent)
    End If

    If qifContent <> "" Then
        qifContent = qifHeader & qifContent
        Call FileAppend(flName & ".qif", qifContent)
    End If

End Sub

Sub FileAppend(fileName As String, content As String)

    Set fso = CreateObject("Scripting.FileSystemObject")
    If Not fso.FileExists(fileName) Then
        Call fso.CreateTextFile(fileName).Close
    End If

    With CreateObject("ADODB.Stream")
        .Type = 2: .Charset = "utf-8": .Open
        .LoadFromFile (fileName)
        .ReadText
        .WriteText content & vbCrLf
        .SaveToFile fileName, 2
        .Close
    End With

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
