# CEP-78 with account whitelisting (modified, enhanced NFT)
This contract is a modified version of Casper's existing `CEP-78 enhanced NFT` contract. The original can be found here:
https://github.com/casper-ecosystem/cep-78-enhanced-nft

## Table of Contents
1. Background Information
   1. Context
   2. Summary of Core Requirements
   3. Why CEP-78?
   4. CEP-78 Changes and Parametrisation
   5. Fulfillment of Core Requirements
2. Code Documentation
   1. Purpose
   2. Modifications
   3. Building and testing on the local environment


## Background Information

### Context

In the scope of this project, a prototype of a real estate investment platform that enables democratization and decentralization
of real estate investments is being implemented. This platform utilizes off-chain smart contracts implemented in the Noumena Protocol
Language (NPL) to handle complex business logic and sensitive data. These off-chain smart-contracts are used to orchestrate
on-chain smart contracts which control actions on the Casper blockchain itself e.g. mint, burn, transfer of tokens representing
the real estate assets, etc.

Milestone 2 aims at creating tokens representing ownership shares of the real estate investment special purpose vehicles (SPVs),
that a) fulfill the business requirements of the involved parties and b) comply with the Swiss DLT law.
This was accomplished by adding functionality to the newly created CEP-78 enhanced NFT standard on the Casper blockchain. It is important
to note, however, that the token is only part of the solution that was developed in order to fulfill these requirements and that
the picture will be completed with the delivery of Milestone 3, 4 and 5.

For a detailed description of the user journeys, please refer to Milestone 1 of this grant. A prototype of the investment platform
will be delivered with Milestone 3 and 4, while a comprehensive white paper on how to utilize smart contracts and tokens for the
digitization and democratization of Real Estate Investments in Switzerland will be delivered with Milestone 5.

### Summary of Core Requirements

The following core requirements have been gathered from a) the involved parties (primarily project developers, investment managers,
banks and investors) and b) the Swiss DLT legislation:

2. #### Obfuscation/Anonymity
   The Token on the blockchain may not hold sensitive information, to ensure the investment and identity related information remains private.
   Most companies for example must not and don't want to disclose their cap table and most investors do not want to disclose their holdings
   or their trades.

3. #### Transfer restrictions
   Legal restrictions with regards to investors such as having to be a professional investor under MIFID II, federal land ownership
   restrictions and other restrictions have to be adhered to.

4. #### Swiss DLT Law
   With the amendment of the law in 2020, Swiss Parliament introduced the register value right (also referred to as Swiss DLT law).
   A register value right must be recorded in a register and a register value right then can only be transferred and enforced via the register.

   The register must meet certain legal requirements and the legislature had DLT systems in mind when it formulated the requirements for the
   register of value. The intention of the DLT law is to make investors as independent as possible from the issuer and any third party.
   In particular they should be able to initiate transactions and look up their holdings directly via the register (DLT) without involvement
   of the issuer and any third party.

   In more detail for a DLT system to be recognized as a register of value, it must meet at least the following four legal requirements
   (Art. 973d para. 2 nOR):
   1. Power of disposal: the holder of the register value right (token holder) must have the power of disposal over his tokens. This
      power of disposition must not only be contractually promised, but also technically guaranteed.
   2. Integrity: The integrity of the stored value register must be protected against unauthorized access by appropriate technical and
      organizational measures against unauthorized changes. As an example, the law mentions the joint management of the register of rights
      to value by several independent parties.
   3. Transparency: the register value right is preceded by an agreement between the parties involved, according to which the register
      right can only be transferred and enforced via the register. The content of the right "securitized" by the register value right
      as well as its features must be clearly formulated and made available to each party either in the DLT system itself or via a link
      that can be called up in the DLT system.
   4. Independent access: the token holder must always be able to access the information and data relating to him without a third party,
      to view and verify the information and register entries relating to him or her.

5. #### Controller action
   A further requirement, specific for tokenized assets, such as shares, is that the issuer of the shares/tokens (contract-installer
   account) may execute specific actions on shares that are held by investors. This allows the controller to perform corporate actions
   (share splits, etc.) on said tokenized assets.

### Why CEP-78?

Initially the project aimed at implementing the real estate token using the ERC-1400 token standard and its concepts
of partitions to obfuscate how many shares of a real estate SPV an investor (account) owns. Partitions are intended to define 
different types of shares, for example ordinary and participation shares. The idea here was, however, to use a partition to represent buckets
of shares, rather than types of shares. This means that a partition becomes equivalent to a token, which represents a
number of shares. The actual number of shares only has to be known by the platform, not the blockchain. It also means, however, that 
various features of the ERC-1400 contract no longer make sense because you cannot specify a number of shares or percentage without 
disclosing some information on the blockchain about how many shares of a real estate SPV an investor (account) owns.

Features that no longer make sense include: balanceOfByPartition, transferWithData, transferFromWithData, transferByPartition,
operatorTransferByPartition, controllerTransfer, controllerRedeem, etc. Basically, any feature that takes an amount of shares
as an argument.

Given that, we found that the best way to maintain privacy is to use a fixed unchangeable token. On creation a number of shares
is assigned to the token via the platform, then on transfer the token must either be transferred as a whole (in case the full 
amount is sold) or burned and two new tokens need to be minted: one transferred to the buyer and the other remaining with the seller (in case a partial amount is sold).

It is also important that tokens should be merged when an account owns more than one token of the same type because otherwise, given
enough trading we tend towards one token representing one share. A way to do this is to use an NFT, and the CEP-78 standard had some 
enhancements on CEP-48 that were useful for us.

While the CEP-47 standard uses `groups`, the CEP-78 standard introduces a new concept of a `whitelist` to control the contracts which
are allowed to mint tokens. While these features were used in very different use cases, there is one fundamental difference. `groups`
control entryPoints and actions based on adding a tag to an account, while whitelists add a list to the given contract, that grants
permissions to the accounts on the list.

Following the existing pattern of the newer CEP-78 standard, the option of using `whitelists`was chosen. However, the standard
whitelist only applied to contracts and the entryPoint of minting. We added an additional whitelist concept to make sure that transfers of
tokens can only succeed for accounts that are whitelisted. The extended `whitelist` concept applies to accounts
and additional entryPoints such as transfer.

### CEP-78 Changes and Parametrisation

This Milestone extends the CEP-78 standard with the following functionality:
- Allow Mint, MetadataUpdate and Burn only for contract-installer account.
- Add whitelist of accounts that tokens can be transferred to.
- Modified transfer method so that it can be invoked by the contract-installer account, given that the target account is on
  the whitelist.

The following list provides a list of the modalities of the CEP-78 standard, the values chosen in this delivery and their rationalization.

| Modality            | Implemented value | Reasoning                                                                                                                                                                                                                                                                               |
|---------------------|-------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Ownership_Mode      | Transferable      | A core requirement of this project is to transfer the CEP-78 NFT between investors qualified for the respective Real Estate Investment (SPV)                                                                                                                                            |
| NFTKind             | Virtual           | The tokens are a digital representation of shares representing an ownership stake of a given real estate project                                                                                                                                                                        |
| NFTHolderMode       | Accounts          | The respective NFTs are held by Investor accounts                                                                                                                                                                                                                                       |
| WhitelistMode       | Locked            | This is only applicable to the contract whitelist that manages the minting of tokens and not to the whitelist that manages the accounts which are cleared for token transfers. Since the minting and burning can only be done by the installer account, the whitelist should be locked. |
| Minting             | Installer         | The ability to mint new tokens is limited to the account that installed the contract                                                                                                                                                                                                    |
| NFTMetadataKind     | Custom            | The custom configuration allows for the storing of a predefined json_schema. This schema will ultimately store the protocol references to the off-chain smart contracts on the Noumena platform                                                                                         |
| NFTIdentifierMode   | Ordinal           | While the original method is more human readable, it also allows for an indication of how many NFTs have already been minted                                                                                                                                                            |
| Metadata_Mutability | Immutable         | The metadata carries information such as the external protocol reference. It is thus essential, that it be immutable                                                                                                                                                                    |
| JSON_Schema         | DummyData         | For the purpose of this Milestone, pure dummy data was provided                                                                                                                                                                                                                         |

### Fulfillment of the Core Requirements

With the enhanced implementation of the CEP-78 standard, we believe that the following core requirements needed for this
project/grant can be achieved:

1. #### Obfuscation/Anonymity
   The Token on the blockchain may not hold sensitive information to ensure the investment and identity related information
   remains private. The NFT in form of the enhanced CEP-78 standard ensures this by representing the total number of shares the investor
   owns and stores this information not on the chain itself, but in a privacy preserving off-chain smart contract on the investment
   platform. The token thus guarantees the ownership, while obfuscating the investment and personal data associated with it.

2. #### Transfer restrictions
   Through the enhancement with the whitelist of accounts, it can be ensured that legal restrictions with regards to investors
   such as having to be a professional investor under MIFID II, federal land ownership restrictions and other restrictions are adhered to.

3. #### Swiss DLT Law compliance
   Using the above-mentioned implementation of the CEP-78 standard with the whitelist functionality, it is possible to ensure
   adherence to the Swiss DLT Law for example in initiating a transfer of tokenized assets on the chain without a dependency on third
   party systems (power of disposal) while ensuring adherence to other regulations such as those with regards to qualified investors.
   The CEP-78 standard also allows to store fingerprints and links to independent services that provide detailed asset information
   (transparency) and owned shares (independent access).

4. #### Controller action
   A further requirement, specific for tokenized assets, such as shares, is that the issuer of the shares/tokens (contract-installer
   account) may execute specific actions on shares that are held by investors. This allows the controller to perform corporate actions
   (share splits, etc.) on said tokenized assets.

This milestone thus not only implemented an enhanced version of the CEP-78 standard, but also conducted a deep analysis on the pros
and cons of using NFTs in the form of CEP-78 instead of ERC-1400 tokens on the blockchain to achieve obfuscation/anonymity and adhere
to transfer restrictions, while maintaining the proof of ownership guaranteed through the token.

## Code Documentation

### Purpose
This contract was implemented to facilitate the tokenization of real estate assets. 

### Modifications
The contract extends the CEP-78 standard with the following functionality:
 - Allow `Mint`, `MetadataUpdate` and `Burn` only for contract-installer account. 
 - Add `whitelist` of accounts that tokens can be transferred to.
 - Add `transferFrom` method that can be invoked only by contract-installer account.

### Building and testing on the local environment
Building and testing of the added functionality on a local machine.

```bash
# Set up the Rust toolchain
make prepare
# Compile the smart contract
make build-contract
# Run integration tests
make clean test
```
