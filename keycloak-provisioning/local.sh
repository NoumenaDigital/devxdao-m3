#!/bin/sh
set -e

sleep 20
terraform init
terraform apply -auto-approve -state=/state/state.tf
