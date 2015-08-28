# Lantern Mobile Specification

Date:   August 28, 2015

Status: Draft

Author: Myles Horton

## Background

[Lantern](https://www.getlantern.org) is widely used on desktops in censored regions, particularly Iran and China, to access web sites that are blocked. 
Lantern relies on a network of people who trust each other (trust network) in
order to distribute information about available proxies in a way that makes it
hard for censors to discover them (see [Kaleidoscope]).  Users are identified
and trusted on the basis of their email addresses (possession is proof of
identity).

In order to relieve the burden of having to share this information manually,
Lantern provides an automated mechanism that allows the Lantern client software
to exchange this information without user intervention.  In order for this to
work, Lantern clients must authenticate with the Lantern cloud infrastructure
so that:

1. The client can supply the trust relationships to the Lantern cloud

2. The Lantern cloud can enforce the trust relationships based on whose Lantern
   is trying to communicate with whom

At the moment, we use Google Sign In to do this.  We have consistently received
negative feedback from users who are unsure of why Google is required to use
Lantern, who may mistrust Google itself, or who may be hesitant to give Lantern
access to their Google account.

The purpose of this LEP is to outline an alternate authentication approach that
doesn't involve Google.

## Proposal

The authentication that is required is done by the Lantern client software that
users install on their machines.  Lantern does not provide any purely web-based
services to which users ever need to sign in.

The Lantern client software is able to maintain its own configuration on disk,
consequently it is not necessary to issue any human-readable/usable credentials
to our users.

All that's really required is to tie the Lantern client to an email address that
is in the user's possession, at which point the Lantern client can start
operating under the identity of that email address.

[PKI] as used in the TLS protocol provides a way to do this.

Brave New Software would operate a centralized certificate authority (CA), which
we'll call "janus", that accepts certificate signing requests (CSRs) from
anyone.  This CA would use a well-guarded and relatively short-lived private
key.

When a user signs in using a Lantern client, she simply enters her email
address. The Lantern client generates a CSR using its own private key,
populating the email address as a [Subject Alternative Name], or SAN.  The
Lantern client then submits this CSR to janus.

When janus receives a CSR, it verifies that the CSR contains an email address
SAN, does not contain a [Subject] and does not contain any other SANs.  If the
certificate meets these criteria, janus issues a certificate and sends an email
to the email address indicated in the SAN.  That email includes a link to the
local Lantern client that encodes the certificate as a URL
(e.g. `http://localhost:15342/AddCert/<PEM Encoded CERT>`). Clicking the link
will automatically import the certificate and display the Lantern UI, at which
point the sign-in process is complete.

In practice, we'll want a lightweight watchdog process that is always running 
(even when Lantern is "off") which can be the one that handles this link.

Now, when the Lantern client communicates with Lantern cloud services like
kscope using TLS, it identifies itself by supplying its certificate as a client
certificate. Since this certificate contains the user's email address as the
SAN, Lantern cloud services know whose behalf the client is working on.

![Sequence Diagram](http://www.websequencediagrams.com/cgi-bin/cdraw?lz=dGl0bGUgU2lnbiBJbiBhbmQgQXV0aGVudGljYXRpb24KClVzZXIgLT4gTGFudGVybjoAJQgKAAoHAA8NQ3JlYXRlIFByaXZhdGUgS2V5AAscQ1NSIHcvIEVtYWlsIFNBTgBHDGphbnVzOiBDU1IKAAYFAAkLVmFsaWQAOQcAEwoAQQU6IExpbmsgd2l0aCBlbWJlZGRlZCBjZXJ0aWZpY2F0ZQCBRQkAJwdDbGljayBvbiBsaW5rCgCBAwYAgWAMT3BlbiBVUkwAgVgVU2F2ZSBDAFILAIIHDGtzY29wZTogVExTIGNvbm5lY3QAgREGAIECDAAfBgAjDElkZW50aWZ5IHVzZXIgYnkAghILABsSRG8gd29yayAuLi4&s=vs2010)

[Source Here](http://www.websequencediagrams.com/?lz=dGl0bGUgU2lnbiBJbiBhbmQgQXV0aGVudGljYXRpb24KClVzZXIgLT4gTGFudGVybjoAJQgKAAoHAA8NQ3JlYXRlIFByaXZhdGUgS2V5AAscQ1NSIHcvIEVtYWlsIFNBTgBHDGphbnVzOiBDU1IKAAYFAAkLVmFsaWQAOQcAEwoAQQU6IExpbmsgd2l0aCBlbWJlZGRlZCBjZXJ0aWZpY2F0ZQCBRQkAJwdDbGljayBvbiBsaW5rCgCBAwYAgWAMT3BlbiBVUkwAgVgVU2F2ZSBDAFILAIIHDGtzY29wZTogVExTIGNvbm5lY3QAgREGAIECDAAfBgAjDElkZW50aWZ5IHVzZXIgYnkAghILABsSRG8gd29yayAuLi4&s=vs2010)

[Kaleidoscope]: http://kscope.news.cs.nyu.edu/pub/TR-2008-918.pdf

[PKI]: http://en.wikipedia.org/wiki/Public_key_infrastructure

[Subject Alternative Name]: http://tools.ietf.org/html/rfc2459#section-4.2.1.7

[Subject]: http://tools.ietf.org/html/rfc2459#section-4.1.2.6

## Usability Benefits

1. The user doesn't have to create a password, which eliminates a step from the
   sign-in flow.

2. The user doesn't have to remember a password

## Engineering Benefits

1. The PKI approach is significantly simpler to implement than password because
   it's completely stateless.  The CA simply maintains its own private key,
   public key and signing certificate.  Literally all it does is accept
   certificate signing requests (CSRs), issue certificates with an email address
   SAN and send those certificates to the corresponding email address.  Contrast
   this with a password-based system that would have to maintain a database of
   users and passwords, still need to send emails to confirm people's email
   address and would also need to provide some sort of password reset facility
   (perhaps using challenge questions, which is more state that needs to be
   stored and UI that needs to be maintained).

2. The PKI approach makes single sign on (SSO) a breeze.  After getting a
   certificate, the client can authenticate with any Lantern cloud service that
   accepts our CA's signing certificate as valid, meaning that kaleidoscope,
   signaling and so on could all authenticate clients without having to maintain
   any state information about users and without having to communicate with some
   sort of sign-on server.  This keeps things very loosely coupled and resilient
   to service interruptions.

3. The PKI approach actually provides a basis for adding additional identity
   providers and, if there's a good reason for it, also giving people the option
   of setting up an account with a password.  In particular, a password or login
   with another identity provider would simply become something that allows your
   Lantern to exchange a CSR for a certificate without having to have it sent to
   you via email.  The really cool thing about this is that the nice SSO
   properties of PKI remain even as we add these other authentication 
   mechanisms.

4. The work that we have to do to support opening urls from emails in Lantern
   has value outside of authentication.  We've talked about sending people
   monthly emails with stats about Lantern and links into the Lantern UI. That
   would benefit from this custom protocol handling capability too.

## Future Enhancements

At some point, we'll need the ability to deactivate/blacklist certain accounts.
There are a couple of ways to do this.

1. Introduce some state to janus that allows trusted Lantern cloud services
   to check the blacklist.

2. Issue time-limited certificates.  Clients can always redeem these from
   janus for newer certificates unless the user account has been
   deactivated.

Option 2 has the benefit of being stateless and not requiring any cooperation between
janus and other cloud servers.  The main downside is that accounts cannot be
immediately deactivated.
