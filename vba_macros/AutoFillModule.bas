Attribute VB_Name = "AutoFillModule"

Private Sub FillActiveSheet()

    Dim lastCell As Range, sheetRange As Range, filterRange As Range, rowRange As Range, fillData As Variant

    fillData = LoadAutoFillData()

    ActiveSheet.AutoFilterMode = False

    Set lastCell = ActiveSheet.Cells.SpecialCells(xlCellTypeLastCell)
    Set sheetRange = ActiveSheet.Range("A2:" + lastCell.Address)

    With sheetRange
        .AutoFilter Field:=1, Criteria1:="<>"
        .AutoFilter Field:=5, Criteria1:="="

        Set filterRange = .SpecialCells(xlCellTypeVisible).EntireRow
        'TODO Function GetRangeToFill

        For Each rowRange In filterRange
            Dim trDate As String
            Dim accountName As String

            trDate = rowRange.Cells(1, c_date).value

            Select Case trDate
            Case "#", ""
                'Skip
            Case "Account"
                accountName = rowRange.Cells(1, c_payee).value
                'Debug.Print accountName
            Case Else
                If accountName Like "4 BankM*" Then
                   Call MTSBPreProcess(rowRange, accountName)
                End If
                If IsEmpty(rowRange.Cells(1, c_category)) Then
                   Call FillPayeeAndCategory(fillData, rowRange)
                End If
            End Select
        Next
    End With

    ActiveSheet.AutoFilterMode = False
End Sub

Private Sub FillPayeeAndCategory(fillData As Variant, rowRange As Range)
    Dim trMessage As String, iNum As Integer, fdMask As String

    trMessage = rowRange.Cells(1, c_comment).value
    trBigMessage = rowRange.Cells(1, c_message).value

    For iNum = 1 To UBound(fillData, 1)
        fdMask = fillData(iNum, 1)

        If (LCase(trMessage) Like LCase(fdMask)) Or (LCase(trBigMessage) Like LCase(fdMask)) Then
            With rowRange
                .Cells(1, c_payee).value = fillData(iNum, 2)
                .Cells(1, c_category).value = fillData(iNum, 3)
                .Cells(1, c_comment).value = fillData(iNum, 5)
                .Cells(1, c_mark).value = IIf(fillData(iNum, 4) <> "", fillData(iNum, 4), "#")
                'Debug.Print .Cells(1, c_date).value & " " & .Cells(1, c_comment).value & " " & fillData(iNum, 2) & " " & fillData(iNum, 3)
            End With
            GoTo EndSub
        End If
    Next iNum

EndSub:

End Sub

Private Sub MTSBPreProcess(rowRange As Range, accountName As String)

    Dim trOperation As String, trMessage As String

    trOperation = rowRange.Cells(1, c_operation).value
    trMessage = rowRange.Cells(1, c_comment).value

    If trOperation Like "~���������� ��*" And accountName = "4 BankM Z" Then
        Dim newPayee As String, newMessage As String

        If trMessage Like "*���������*" Then
            newPayee = "�������� - �����"
            newMessage = "���������"
        ElseIf trMessage Like "*�����*" Then
            newPayee = "�������� - �����"
            newMessage = ""
        Else
            newPayee = "��������"
            newMessage = ""
        End If

        rowRange.Cells(1, c_comment).value = newMessage
        rowRange.Cells(1, c_payee).value = newPayee
        rowRange.Cells(1, c_category).value = "��������"
        rowRange.Cells(1, c_mark).value = "#"

    ElseIf trOperation Like "~��������*" Then
        rowRange.Cells(1, c_category).value = "����. �������"
        rowRange.Cells(1, c_payee).value = "����. ������������"
        rowRange.Cells(1, c_mark).value = "#"

    ElseIf trOperation = "~��������" And trMessage = "������� � ����� �� ����" Then
        Call FillBankTransfer(rowRange, "[8 BankM D]")

    ElseIf trOperation = "~����������" And trMessage = "������� ����� �������" Then
        Call FillBankTransfer(rowRange, "[8 BankM D]")

    ElseIf accountName = "4 BankM Z" And trOperation = "�������� � ���������" And trMessage = "PEREVOD NA KARTU MTSB" Then
        Call FillBankTransfer(rowRange, "[4 BankM]")

    ElseIf accountName = "4 BankM" And trOperation = "�������� � ���������" And trMessage = "PEREVOD NA KARTU MTSB" Then
        Call FillBankTransfer(rowRange, "[4 BankM V]")

    ElseIf accountName = "4 BankM" And trOperation = "���������� �� ��������" And trMessage = "PEREVOD NA KARTU MTSB" Then
        Call FillBankTransfer(rowRange, "[4 BankM V]")

    ElseIf accountName = "4 BankM V" And trMessage = "PEREVOD NA KARTU MTSB" Then
        Call FillBankTransfer(rowRange, "[4 BankM]")

    End If

End Sub

Private Sub FillBankTransfer(rowRange As Range, account As String)
    With rowRange
        .Cells(1, c_category).value = account
        .Cells(1, c_comment).value = ""
        .Cells(1, c_mark).value = "#"
    End With
End Sub

Function LoadAutoFillData() As Variant

    Set ws = Workbooks(BOOK_HISTORY).Worksheets("AutoFill")

    Set lastCell = ws.Cells.SpecialCells(xlCellTypeLastCell)
    Set sheetRange = ws.Range("A2:" + lastCell.Address)

    LoadAutoFillData = sheetRange
End Function

