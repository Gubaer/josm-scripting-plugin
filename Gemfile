source "https://rubygems.org"

# Jekyll 4.x runs on Ruby 3.2 and uses Liquid 5.x, which no longer has the
# tainted?/untrusted? methods removed in Ruby 3.2.
gem "jekyll", "~> 4.3"

gem "just-the-docs"

# If you have any plugins, put them here!
group :jekyll_plugins do
  gem "jekyll-include-cache"
  gem "jekyll-seo-tag"
end

# Windows does not include zoneinfo files, so bundle the tzinfo-data gem
# and associated library.
install_if -> { RUBY_PLATFORM =~ %r!mingw|mswin|java! } do
  gem "tzinfo", "~> 1.2"
  gem "tzinfo-data"
end
