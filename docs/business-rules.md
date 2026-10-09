# RentHub Business Rules

This document describes the current domain rules enforced by the application. The executable sources of truth remain the database constraints/configuration, domain services, controllers and tests.

## Booking lifecycle

- New bookings start as `PENDING`.
- `PENDING` bookings reserve inventory and can receive compatible add-on services.
- Successful payment moves a booking to `PAID`.
- Paid bookings cannot use direct cancellation; they enter the refund workflow.
- Refund requests move a booking to `REFUND_PENDING`; approval moves it to `REFUND_APPROVED`; payout of the refund moves it to `REFUNDED`. Rejection returns it to `PAID`.
- Completed trips move to `COMPLETED` and become eligible for owner payout and reviews.
- Unpaid pending bookings may become `EXPIRED` after the configured timeout.
- Every status transition is written to `RH_BOOKING_HISTORY`.

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
- A coupon can only be applied to a `PENDING` booking and is governed by date, minimum amount, global usage and per-user usage limits.
- Add-on services are priced separately from the booking amount.
- Car bookings may include a deposit.
- Payment captures: booking amount after coupon + add-ons + deposit.
- Payment attempts are recorded independently from successful payments so declined/failed provider attempts remain observable.

## Availability

A listing is unavailable when either of these overlaps the requested dates:

- an existing blocking booking state;
- an owner/admin availability block, including maintenance-generated blocks.

Listing status must also be `AVAILABLE` before booking.

## Cancellation and refunds

Cancellation fee tiers are stored in `RH_CANCELLATION_POLICIES`; Java does not own the percentages.

The active policy is selected by asset type, priority and hours remaining before the booking start. The selected rule produces the fee and refundable amount before a refund is created. Customers can request a quote before submitting the refund request.

## Ownership and roles

- Customers can act only on their own bookings, favorites, documents, tickets, disputes and notifications.
- Owners can manage only inventory they own, including maintenance and availability blocks.
- Admin-only operations include refund decisions, document verification, platform pricing rules, cancellation policy configuration, system configuration, payout completion and audit access.

## Traceability

Important state changes write audit events and customer-facing notifications. Booking status transitions additionally write `RH_BOOKING_HISTORY`, while payment provider calls write `RH_PAYMENT_ATTEMPTS`.
