C:\ZsZs\EntwicklungsUmgebung\jira-cli-3.9.0\jira.bat --server http://localhost:2990/jira --user admin --password admin --action run --file createJiraProjectsAndUsers.txt --findReplace @project@:WORKLOG,@user@:admin,@password@:admin --continue

C:\ZsZs\EntwicklungsUmgebung\jira-cli-3.9.0\jira.bat --server http://localhost:2990/jira --user admin --password admin --action run --file createJiraIssues.txt --findReplace @project@:WORKLOG,@user@:GermanEmployee,@password@:password --continue
