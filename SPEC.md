# Lantern Mobile Specification

Date:   August 28, 2015

Status: Draft

Author: Myles Horton

## Background

[Lantern](https://www.getlantern.org) is widely used on desktops in censored regions, particularly Iran and China, to access web sites that are blocked. In those regions mobile is increasingly the dominant way for people to access the Internet, however, and users have requested Lantern on mobile in both two user surveys and in less formal feedback on social media and discussion forums. Lantern Mobile is an effort to meet this demand.


## Proposal

The Lantern team initially proposes to build Lantern Mobile on Android only. The initial MVP will use the always-on full device VPN mode introduced in Android 4.2 to proxy all network connections on the device and will not support older versions of Android. It will then interface directly with the existing [Lantern backend](https://github.com/getlantern/lantern) written in the Go programming language to send that data through the Lantern proxying system, complete with its various blocking resistance features. The details of those features are outside the scope of this document but include domain-fronting, peer-to-peer, and more traditional chained proxies. The first version will only unblock access to HTTP traffic, and particularly only to HTTP destinations that are detected as blocked. All other traffic will be forwarded directly to its original destination without going through the Lantern infrastructure.

The user interface for Lantern Mobile must be simple and easy to use. This document includes initial designs for that interface, although the real-world usability will be tested continually with users and will undoubtedly change in response to user feedback.

## Full Device VPN 

When the user installs Lantern, he or she will be prompted to allow Lantern to run in full device VPN mode. Assuming the user approves that action, Lantern will then start intercepting all network traffic on the device. This is markedly different from Lantern behavior on desktops where Lantern only intercepts HTTP traffic that adheres to system proxy settings. It differs in that on mobile Lantern will not only receive non-HTTP traffic, but it also receives non-TCP traffic. Beyond that, Lantern on mobile receives the raw IP packets themselves and must parse and interpret each one.

## User Interface


