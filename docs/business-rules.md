# RentHub Business Rules

This document describes the current domain rules enforced by the application. The executable sources of truth remain the database constraints/configuration, domain services, controllers and tests.

## Booking lifecycle

- New bookings start as `PENDING`.
- `PENDING` bookings reserve inventory and can receive compatible add-on services.
- Successful payment moves a booking to `PAID`.
- Paid bookings cannot use direct cancellation; they enter the refund workflow.
- Refund requests move a booking to `REFUND_PENDING`; approval moves it to `REFUND_APPROVED`; payout of the refund moves it to `REFUNDED`. Rejection returns it to `PAID`.
- Completed trips move to `COMPLETED` and become eligible for owner settlement review and reviews.
- Unpaid pending bookings may become `EXPIRED` after the configured timeout.
- Every booking status transition is written to `RH_BOOKING_HISTORY`.

## Service compatibility

`RH_EXTRA_SERVICES.APPLICABLE_TO` is the compatibility source of truth:

- `CAR`: service can only be attached to car bookings.
- `PROPERTY`: service can only be attached to property/stay bookings.
- `BOTH`: service can be attached to either type.

The rule is enforced in the backend even if a client bypasses UI filtering. The marketplace UI also filters choices so invalid combinations are not presented.

Current seeded examples:

- Private Driver -> `CAR`
- Baby Seat -> `CAR`
- Roadside Support -> `CAR`
- Property Cleaning -> `PROPERTY`
- Portable Wi-Fi -> `BOTH`
- Airport Transfer -> `BOTH`

## Pricing and payment

- Base booking amount is derived from duration x listing rate.
- Active pricing rules may increase or decrease the booking amount before booking creation.
- The pricing rules actually applied at booking creation are snapshotted in `RH_BOOKING_PRICE_ADJ`; later pricing-rule edits do not erase the original decision trace.
- The booking pricing trace is exposed through `/api/bookings/{bookingId}/pricing` to the booking owner and admins.
- A coupon can only be applied to a `PENDING` booking and is governed by date, minimum amount, global usage and per-user usage limits.
- Add-on services are priced separately from the booking amount.
- Car bookings may include a deposit.
- Payment captures: booking amount after coupon + add-ons + deposit.
- Payment attempts are recorded independently from successful payments so declined/failed provider attempts remain observable.

## Availability and maintenance

A listing is unavailable when either of these overlaps the requested dates:

- an existing blocking booking state;
- an owner/admin availability block, including maintenance-generated blocks.

Listing status must also be `AVAILABLE` before booking.

An owner/admin cannot create a manual availability block or schedule maintenance over an active booking. Maintenance creates a linked `MAINTENANCE` availability block. Completing or cancelling the maintenance record removes that linked block. Maintenance blocks cannot be deleted directly through the generic availability endpoint; their lifecycle is owned by the maintenance workflow.

## Check-in compliance

Check-in is not only date/status driven; it also has document eligibility rules stored in system configuration.

- Car check-in requires a verified, non-expired `DRIVING_LICENSE` by default.
- Property check-in requires at least one verified, non-expired identity document (`NATIONAL_ID` or `PASSPORT`) by default.
- Required document types can be changed through `RH_SYSTEM_CONFIG` without changing Java code.

## Cancellation and refunds

Cancellation fee tiers are stored in `RH_CANCELLATION_POLICIES`; Java does not own the percentages.

The active policy is selected by asset type, priority and hours remaining before the booking start. The selected rule produces the fee and refundable amount before a refund is created. Customers can request a quote before submitting the refund request.

Refunds participate in owner-settlement safety: requested, approved and paid refunds block owner payout for that booking. A rejected refund may release a payout only when no other financial hold remains.

## Disputes and payout holds

- A dispute can only be opened after payment-related booking states are reached; `PENDING` bookings are not disputable.
- Only one active dispute (`OPEN` or `UNDER_REVIEW`) is allowed per booking.
- Opening a dispute places any existing owner payout on `ON_HOLD`.
- If a payout does not exist yet, the later payout creation checks dispute/refund state and starts on hold when necessary.
- Closing a dispute releases a payout only when no other active dispute or payout-blocking refund remains.
- Admin payout completion is rejected while any financial hold exists.

## Support SLA

Support response targets are configuration-driven:

- `support.low.sla.hours`
- `support.normal.sla.hours`
- `support.high.sla.hours`
- `support.urgent.sla.hours`

Each support ticket stores `SLA_DUE_AT`, `SLA_STATUS` and the first non-internal admin response timestamp. A scheduler refreshes open ticket SLA state, and a late first response remains `BREACHED`; an on-time first response becomes `MET`.

## Ownership and roles

- Customers can act only on their own bookings, favorites, documents, tickets, disputes and notifications.
- Owners can manage only inventory they own, including maintenance and availability blocks.
- Admin-only operations include refund decisions, document verification, platform pricing rules, cancellation policy configuration, system configuration, payout completion and audit access.

## Traceability

Important state changes write audit events and customer-facing notifications. Booking status transitions additionally write `RH_BOOKING_HISTORY`, payment provider calls write `RH_PAYMENT_ATTEMPTS`, pricing decisions write `RH_BOOKING_PRICE_ADJ`, maintenance stores the block it created, support tickets persist SLA state and financial workflows can move payouts into explicit hold/release states.
