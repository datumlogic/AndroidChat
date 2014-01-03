AndroidChat prototype

when a user adds contact/favorite to his list, this is the ‘normal’ XMPP flow:

User A adds user B to contact list
User B accepts tUser A's contact request and sends User A a request of his own
User A accepts User B’s request

I am making an option that assumes that User A will accept User B’s, by virtue of the fact that he added him to his Favorite list. This take stye normal workflow of:

User A adds User B to contact list, and ‘pre-accepts’ User B’s request
User B accepts User A\s contact request