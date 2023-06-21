Attribute VB_Name = "AutoFillModule"

Sub FillActiveSheet()

    Dim lastCell As Range, sheetRange As Range, filterRange As Range, rowRange As Range, fillData As Variant

    fillData = LoadAutoFillData()

    ActiveSheet.AutoFilterMode = False

    Set lastCell = ActiveSheet.Cells.SpecialCells(xlCellTypeLastCell)
    Set sheetRange = ActiveSheet.Range("$A$2:" + lastCell.Address)

    With sheetRange
        .AutoFilter Field:=1, Criteria1:="<>"
        .AutoFilter Field:=5, Criteria1:="="

        Set filterRange = .SpecialCells(xlCellTypeVisible).EntireRow
        'TODO Function GetRangeToFill

        For Each rowRange In filterRange
            Dim trDate As String
            Dim accountName As String

            trDate = rowRange.Cells(1, 1).value

            Select Case trDate
            Case "#", ""
                'Skip
            Case "Account"
                accountName = rowRange.Cells(1, 4).value
                'Debug.Print accountName
            Case Else
                If accountName Like "4 BankM*" Then
                   Call MTSBPreProcess(rowRange, accountName)
                End If
                If IsEmpty(rowRange.Cells(1, 5)) Then
                   Call FillPayeeAndCategory(fillData, rowRange)
                End If
            End Select
        Next
    End With

    ActiveSheet.AutoFilterMode = False
End Sub

Private Sub FillPayeeAndCategory(fillData As Variant, rowRange As Range)
    Dim trMessage As String, iNum As Integer, fdMask As String

    trMessage = rowRange.Cells(1, 2).value
    trBigMessage = rowRange.Cells(1, 13).value

    For iNum = 1 To UBound(fillData, 1)
        fdMask = fillData(iNum, 1)

        If (LCase(trMessage) Like LCase(fdMask)) Or (LCase(trBigMessage) Like LCase(fdMask)) Then
            With rowRange
                .Cells(1, 4).value = fillData(iNum, 2)
                .Cells(1, 5).value = fillData(iNum, 3)
                .Cells(1, 2).value = ""
                .Cells(1, 6).value = "#"
                'Debug.Print .Cells(1, 1).value & " " & .Cells(1, 2).value & " " & fillData(iNum, 2) & " " & fillData(iNum, 3)
            End With
            GoTo ContinueFor
        End If
    Next iNum

ContinueFor:

End Sub

Private Sub MTSBPreProcess(rowRange As Range, accountName As String)

    Dim trOperation As String, trMessage As String

    trOperation = rowRange.Cells(1, 7).value
    trMessage = rowRange.Cells(1, 2).value

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

        rowRange.Cells(1, 5).value = "��������"
        rowRange.Cells(1, 4).value = newPayee
        rowRange.Cells(1, 2).value = newMessage

    ElseIf trOperation Like "~��������*" Then
        rowRange.Cells(1, 5).value = "����. �������"
        rowRange.Cells(1, 4).value = "����. ������������"
        rowRange.Cells(1, 6).value = "#"

    ElseIf trOperation = "~��������" And trMessage = "������� � ����� �� ����" Then
        Call FillBankTransfer(rowRange, "[8 BankM D]")

    ElseIf trOperation = "~����������" And trMessage = "������� ����� �������" Then
        Call FillBankTransfer(rowRange, "[8 BankM D]")

    ElseIf trOperation = "�������� � ���������" And trMessage = "PEREVOD NA KARTU MTSB" And accountName = "4 BankM Z" Then
        Call FillBankTransfer(rowRange, "[4 BankM]")

    ElseIf trOperation = "���������� �� ��������" And trMessage = "PEREVOD NA KARTU MTSB" And accountName = "4 BankM" Then
        Call FillBankTransfer(rowRange, "[4 BankM Z]")

    End If

End Sub

Private Sub FillBankTransfer(rowRange As Range, account As String)
    With rowRange
        .Cells(1, 5).value = account
        .Cells(1, 2).value = ""
        .Cells(1, 6).value = "#"
    End With
End Sub

Function LoadAutoFillData() As Variant
    Dim lastCell As Range, sheetRange As Range

    Set lastCell = Worksheets("AutoFill").Cells.SpecialCells(xlCellTypeLastCell)
    Set sheetRange = Worksheets("AutoFill").Range("$A$2:" + lastCell.Address)

    LoadAutoFillData = sheetRange
End Function

