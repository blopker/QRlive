#!/usr/bin/env bash

rsync -ahvc --delete project_site/* gouda@blopker.com:public/dev/qrlive
