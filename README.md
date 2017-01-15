# LedgerJournal
**keep your journals up-to-date with a simple Android App**

This is an Android App to create transactions for John Wiegley's great double-entry accounting
system [Ledger](http://ledger-cli.org/).

I use Ledger to keep track of all my expenses and income.
Ledger is based on transactions stored in simple text files.
All transactions involving a bank can be imported automatically from online banking.
I use [icsv2ledger](https://github.com/quentinsf/icsv2ledger) for this.
But what's about cash transactions? I wrote this app to be able to make a note of all cash transactions
immediately and then just export them in Ledger format.

## Features
- create multiple journals
- add and edit transactions with up to 4 postings
- autocompletion of accounts for known payees (from templates)
- choose known payees and accounts from dropdown lists
- save any transaction as template
- edit templates
- export and share ledger text file
