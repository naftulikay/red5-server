# JRuby - style
require 'java'
module RedFive
    include_package "org.red5.server.api"
    include_package "org.springframework.core.io"
end

#
# demoservice.rb - a translation into Ruby of the olfa demo application, a red5 example.
#
# @author Paul Gregoire
#
# http://www.rubycentral.com/ref/ref_c_array.html#assoc
# http://www-128.ibm.com/developerworks/java/library/j-alj09084/
class DemoService

	def initialize
	   puts "Initializing ruby demoservice"
	end

	def getListOfAvailableFLVs
		puts "getListOfAvailableFLVs"
		@filesMap = {}
		puts "Getting the FLV files"
		begin
			flvs = Red5.getConnectionLocal.getScope.getResources("streams/*.flv")
			for flv in flvs
				file = flv.getFile
				lastModified = formatDate(Time.at(file.lastModified))
				#File.mtime("testfile")
				flvName = file.getName
				flvBytes = file.length
				#File.size("testfile")
				puts "FLV Name: #{flvName}"
				puts "Last modified date: #{lastModified}"
				puts "Size: #{flvBytes}"
				puts "-------"
				@fileInfo = {}
				fileInfo["name"] = flvName
				fileInfo["lastModified"] = lastModified
				fileInfo["size"] = flvBytes
				filesMap[flvName] = fileInfo
			end
		rescue
			puts "Error in getListOfAvailableFLVs"
		end
		return filesMap
	end

	def formatDate(date)
		puts "formatDate"
		return date.strftime("%d/%m/%Y %I:%M:%S")
	end

    def method_missing(m, *args)
      super unless @value.respond_to?(m) 
      return @value.send(m, *args)
    end

end
