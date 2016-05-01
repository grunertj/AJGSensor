{
  if ($1 == "<trkpt") {
    line = $0
  } else if ($1 ~ /<time>/) {
    line = line $0
  } else if ($1 ~ /<hdop>/) {
    line = line $0
  } else if ($1 ~ /<extensions>/) {
    line = line $0
  } else if ($1 ~ /<\/extensions>/) {
    line = line $0
  } else if ($1 ~ /<speed>/) {
    line = line $0
  } else if ($1 == "</trkpt>") {
    line = line $0
    if ( line ~ /speed/) {
      print line
    }
  } else {
    print $0
  }
}
