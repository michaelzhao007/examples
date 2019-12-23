// StreamingContributors.sc

val gitLog = os.proc("git", "log").spawn()
val grepAuthor = os.proc("grep", "Author: ").spawn(stdin = gitLog.stdout)
val output = grepAuthor.stdout.lines.distinct
pprint.log(output)