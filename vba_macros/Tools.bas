Attribute VB_Name = "Tools"

Sub BackupModules()

    Dim VBComponent As Object
    Dim path As String
    Dim directory As String
    Dim fso As Object

    directory = ActiveWorkbook.path & "\arc\macro_" & Format(Now(), "yyyy-mm-dd-hhmmss")
    Response = MsgBox("Экспортируем макросы PERSONAL.XLSB в Git:" + vbCrLf + "Да - " + BACKUP_TO_GIT_PATH + vbCrLf + "Нет - " + directory, vbYesNoCancel + vbQuestion, "Уверен?")

    Select Case Response
        Case vbCancel
            Exit Sub
        Case vbYes
            directory = BACKUP_TO_GIT_PATH
        Case vbNo
            Set fso = CreateObject("Scripting.FileSystemObject")
            If Not fso.FolderExists(directory) Then
                Call fso.CreateFolder(directory)
            End If
        Set fso = Nothing
    End Select

    count = 0
    'On security error set trust: https://support.microsoft.com/en-us/office/enable-or-disable-macros-in-microsoft-365-files-12b036fd-d140-4e74-b45e-16fed1a7e5c6
    For Each VBComponent In Workbooks("PERSONAL.XLSB").VBProject.VBComponents
        If VBComponent.Type = 1 And Not VBComponent.Name = "MailTools" Then
            Debug.Print VBComponent.Name
            path = directory & "\" & VBComponent.Name & ".bas"
            Call VBComponent.Export(path)

            If Err.Number = 0 Then
                count = count + 1
            Else
                Call MsgBox("Failed to export " & VBComponent.Name & " to " & path, vbCritical)
            End If
        End If
    Next

    'Call MsgBox("Successfully exported " & CStr(count) & " VBA files to " & directory)
End Sub

Private Sub FixMonth()

    Dim findValues, replaceValues As Variant
    findValues = Array("января", "февраля", "марта", "апреля", "мая", "июня", "июля", "августа", "бря")
    replaceValues = Array("январь", "февраль", "март", "апрель", "май", "июнь", "июль", "август", "брь")

    For i = 0 To 8
        ActiveSheet.Cells.Replace What:=findValues(i), Replacement:=replaceValues(i)
    Next i

End Sub

