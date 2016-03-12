# Exporting an MBox from Email

One easy place to obtain a communications network to perform graph analyses is your email. Tribe extracts the relationships between unique email addresses by exploring who is connected by participating in the same email address. In particular, we will use a common format for email storage called `mbox`. If you have Apple Mail, Thunderbird, or Microsoft Outlook, you should be able to export your mbox. If you have [Gmail](https://gmail.com) you may have to use an online email extraction tool.

## Gmail or Google Apps

**Note, if you're taking the DDL Workshop, make sure you do this in advance of the class, it can take hours or even days for the archive to be created!**

1. Go to [https://takeout.google.com/settings/takeout](https://takeout.google.com/settings/takeout).
2. In the "select data to include" box, make sure Mail is turned on and everything else is turned off, then click Next.
3. Select your compression format (zip for Windows, tgz for Mac) and click Create Archive.
4. Once the archive has been created, you will receive an email notification.

## Outlook
1. Select the messages you would like to export, or the folder, if you would like to export the entire folder.
2. Click the MessageSave Outlook toolbar button.
3. Select "include subfolders" if you would like to export subfolders of the current folder as well.
4. Select "MBOX" in the "Format" field.Click "Save Now".
5. That's it. You should see mbox file(s) created in the destination directory.
6. MessageSave creates one file per Outlook folder processed.

## Thunderbird

1. Go to the [Import/Export Tools website](https://addons.mozilla.org/en-US/thunderbird/addon/importexporttools/).
2. Right-click on the download link and select "Save Target/Link As."
3. Save the ".xpi" file to your computer's hard disk and note the location.
4. Start up Thunderbird and select "Add-ons" from the "Tools" menu.
5. Click "Extensions" in the new window and click "Install."
6. Browse to your saved ImportExport Tools ".xpi" file and click "Open."
7. Click the "Install Now" button and close Thunderbird.
8. Restart Thunderbird and select "ImportExport Tools" from the "Tools" menu. Click "Options."
9. Select the "Export Directories" tab. Check the box next to "Export folders as MBOX file."
10. Browse to the drive and folder to which you want to export your mbox files. Click "OK" twice.
11. Select "ImportExport Tools" from the "Tools" menu again. Click on "Export all the folders."
12. Choose a folder from Thunderbird's collective "Profiles" folder and its contents will be exported as mbox files.

## Apple Mail

1. Select one or more mailboxes to export.

    To select mailboxes that are next to each other (contiguous) in the list, hold down Shift as you click the first and last mailbox. To select mailboxes that are not next to each other in the list, hold down Command as you click each mailbox.

2. Choose Mailbox > Export Mailbox, or choose Export Mailbox from the Action pop-up menu (looks like a gear) at the bottom of the sidebar.
3. Choose a folder or create a new folder where you want to store the exported mailbox, and then click Choose.

    Mail exports the mailboxes as .mbox packages. If you previously exported a mailbox, Mail does not overwrite the existing .mbox file but appends a number to the filename of the new export to create a new version, such as My Mailbox 3.mbox.
